package ch.inftec.ju.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.DataHolder;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.JuUtils;
import ch.inftec.ju.util.PropertyChain;
import ch.inftec.ju.util.RegexUtil;

/**
 * Utility functions related to JuConnUtil.
 * @author Martin
 *
 */
public class JuConnUtils {
	/**
	 * Gets a builder to create a new instance of JuConnUtils.
	 * @return
	 */
	public static JuConnUtilBuilder build() {
		return new JuConnUtilBuilder();
	}
	
	/**
	 * Creates a JuConnUtil instance for a DbWorker implementation.
	 * @param worker DbWorker
	 * @return JuConnUtil
	 */
	public static JuConnUtil createByDbWorker(final DbWorker worker) {
		return new AbstractJuConnUtil() {
			@Override
			protected void performDbWork(DbWork work) throws SQLException {
				worker.doWork(work);
			}
		};
	}
	
	/**
	 * Creates a JuConnUtil wrapper around a raw JDBC connection.
	 * @param conn Connection
	 * @return JuConnUtil
	 */
	public static JuConnUtil createByConnection(final Connection conn) {
		return new AbstractJuConnUtil() {
			@Override
			protected void performDbWork(DbWork work) throws SQLException {
				work.execute(conn);
			}
		};
	}
	
	/**
	 * Creates a JuConnUtil wrapper around a JDBC DataSource.
	 * @param ds DataSource
	 * @return JuConnUtil
	 */
	public static JuConnUtil createByDataSource(final DataSource ds) {
		return new AbstractJuConnUtil() {
			@Override
			protected void performDbWork(DbWork work) throws SQLException {
				try (Connection conn = ds.getConnection()) {
					work.execute(conn);
				}
			}
		};
	}
	
	/**
	 * Creates a JuConnUtil wrapper around a JDBC DataSource.
	 * <p>
	 * Allows to specify whether to reuse the same connection all the time. This can be useful
	 * for performance improvements, e.g. in testing contexts...
	 * @param ds DataSource
	 * @param recycleConnection If true, the same connection will be recycled (and never closed!)
	 * @return JuConnUtil
	 */
	public static JuConnUtil createByDataSource(final DataSource ds, boolean recycleConnection) {
		if (!recycleConnection) {
			return JuConnUtils.createByDataSource(ds);
		} else {
			try {
				final Connection conn = ds.getConnection();
			
				return new AbstractJuConnUtil() {
					@Override
					protected void performDbWork(DbWork work) throws SQLException {
						work.execute(conn);
					}
				};
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't get Connection from DataSource", ex);
			}
		}
	}
	
	/**
	 * Returns a JdbcTemplate instance that uses the specified Connection.
	 * <p>
	 * The close method will not be called explicitly.
	 * @param conn Connection to be used for DB calls
	 * @return JdbcTemplate wrapper
	 */
	public static JdbcTemplate asJdbcTemplate(Connection conn) {
		return new JdbcTemplate(new SingleConnectionDataSource(conn, true));
	}
	
	/**
	 * Helper class to create JuConnUtil instances.
	 * @author Martin
	 *
	 */
	public static final class JuConnUtilBuilder {
		private String url;
		private String user;
		private String password;
		
		private JuConnUtilBuilder() {}
		
		/**
		 * Set the JDBC URL to connect to the DB
		 * @param url JDBC URL
		 * @return
		 */
		public JuConnUtilBuilder url(String url) {
			this.url = url;
			return this;
		}
		
		/**
		 * Sets the DB user name used to connect to the DB.
		 * @param user User name
		 * @return
		 */
		public JuConnUtilBuilder user(String user) {
			this.user = user;
			return this;
		}
		
		/**
		 * Sets the password to connect to the DB.
		 * @param password DB Password
		 * @return
		 */
		public JuConnUtilBuilder password(String password) {
			this.password = password;
			return this;
		}
		
		/**
		 * Loads connection info by PropertyChain profile.
		 * <p>
		 * This will look for the following properties:
		 * <ul>
		 *   <li>ju-dbutil-test.[profileName].connectionUrl</li>
		 *   <li>ju-dbutil-test.[profileName].user</li>
		 *   <li>ju-dbutil-test.[profileName].password</li>
		 * </ul>
		 * @param profileName
		 * @return
		 */
		public JuConnUtilBuilder profile(String profileName) {
			PropertyChain pc = JuUtils.getJuPropertyChain();
			
			return this.url(pc.get(String.format("ju-dbutil-test.%s.connectionUrl", profileName)))
				.user(pc.get(String.format("ju-dbutil-test.%s.user", profileName)))
				.password(pc.get(String.format("ju-dbutil-test.%s.password", profileName)));
		}
		
		/**
		 * Creates a new JuConnUtil instance as configured using the builder
		 * @return
		 */
		public JuConnUtil create() {
			AssertUtil.assertNotEmpty("DB URL hasn't been set", this.url);
			
			return new JdbcConnUtil(this.url, this.user, this.password);
		}
	}
	
	private abstract static class AbstractJuConnUtil implements JuConnUtil {
		private DbType dbType;
		
		@Override
		public <T> T extractDatabaseMetaData(final DatabaseMetaDataCallback<T> action) {
			final DataHolder<T> res = new DataHolder<>();
			
			this.doWork(new DbWork() {
				@Override
				public void execute(Connection connection) {
					try {
						DatabaseMetaData metaData = connection.getMetaData();
						res.setValue(action.processMetaData(metaData));
					} catch (Exception ex) {
						throw new JuRuntimeException("Couldn't get JDBC MetaData", ex);
					}
				}
			});
			
			return res.getValue();
		}
		
		@Override
		public DbType getDbType() {
			if (this.dbType == null) {
				String productName = this.extractDatabaseMetaData(new DatabaseMetaDataCallback<String>() {
					@Override
					public String processMetaData(DatabaseMetaData dbmd) throws SQLException {
						return dbmd.getDatabaseProductName();
					}
				});
				
				this.dbType = DbType.evaluateDbType(productName);
			}
			
			return this.dbType;
		}
		
		@Override
		public void doWork(DbWork work) {
			try {
				this.performDbWork(work);
			} catch (Exception ex) {
				throw new JuRuntimeException("Couldn't execute DB work", ex);
			}
		}
		
		/**
		 * Extending classes need to provide an implementation of performDbWork to execute
		 * DB Work in a JDBC Connection.
		 * @param work Work to be executed
		 * @throws SQLException If execution fails
		 */
		protected abstract void performDbWork(DbWork work) throws SQLException;
		
		@Override
		public void doWork(final DsWork work) {
			this.doWork(new DbWork() {
				@Override
				public void execute(Connection conn) {
					work.execute(new SingleConnectionDataSource(conn, false));
				}
			});
		}
		
		@Override
		public MetaDataInfo getMetaDataInfo() {
			return this.extractDatabaseMetaData(new DatabaseMetaDataCallback<MetaDataInfo>() {
				@Override
				public MetaDataInfo processMetaData(DatabaseMetaData dbmd) throws SQLException {
					return new MetaDataInfoImpl(dbmd.getUserName(), dbmd.getURL(), AbstractJuConnUtil.this);
				}
			});
		}
		
		@Override
		public DbHandler getDbHandler() {
			return this.getDbType().getDbSpecificHandler(this);
		}
	}
	
	private static final class MetaDataInfoImpl implements MetaDataInfo {
		private final String userName;
		private final String url;
		private final JuConnUtil connUtil;
		
		public MetaDataInfoImpl(String userName, String url, JuConnUtil connUtil) {
			this.userName = userName;
			this.url = url;
			this.connUtil = connUtil;
		}
		
		@Override
		public String getUrl() {
			return this.url;
		}
		
		@Override
		public String getUserName() {
			return this.userName;
		}
		
		@Override
		public SchemaInfo getSchemaInfo() {
			if (this.connUtil.getDbType() == DbType.MYSQL) {
				// Get the Schema info from the URL (has the form jdbc:mysql://host/schema)
				RegexUtil ru = new RegexUtil(".*/([^/]+)");
				if (!ru.matches(this.getUrl())) {
					return null;
				} else {
					String schemaName = ru.getMatches(this.getUrl())[0].getGroups()[0];
					return new SchemaInfoImpl(schemaName, null);							
				}
			} else if (this.connUtil.getDbType() == DbType.ORACLE){
				return new SchemaInfoImpl(this.getUserName(), null);
			} else {
				throw new UnsupportedOperationException("Not yet implemented for " + this.connUtil.getDbType());
			}
		}
		
		@Override
		public List<String> getPrimaryKeyColumns(String tableName) {
			final String actualTableName = this.connUtil.getDbType().getDbSpecificHandler(this.connUtil).convertTableNameCasing(tableName);
			final SchemaInfo schemaInfo = getSchemaInfo();
			
			List<String> columnNames = this.connUtil.extractDatabaseMetaData(new DatabaseMetaDataCallback<List<String>>() {
				@Override
				public List<String> processMetaData(DatabaseMetaData dbmd) throws SQLException {
					
					ResultSet rs = dbmd.getPrimaryKeys(schemaInfo.getCatalog(), schemaInfo.getName(), actualTableName);
					
					List<String> columnNames = new ArrayList<>();
					while (rs.next()) {
						columnNames.add(rs.getString("COLUMN_NAME"));
					}
					rs.close();
					
					return columnNames;
				}
			});
			
			return columnNames;		
		}
		
		@Override
		public SchemaInfos getSchemaInfos() {
			SchemaInfos schemaInfos = null;
			
			// Note: MySQL returns an empty list when calling getSchemas. Schemas are returned by the getCatalogs method instead...
			if (this.connUtil.getDbType() == DbType.MYSQL) {
				schemaInfos = this.connUtil.extractDatabaseMetaData(new DatabaseMetaDataCallback<SchemaInfos>() {
					@Override
					public SchemaInfos processMetaData(DatabaseMetaData dbmd) throws SQLException {
						SchemaInfos schemaInfos = new SchemaInfos();
						
						ResultSet rs = dbmd.getCatalogs();
						
						while (rs.next()) {
							String name = rs.getString("TABLE_CAT");
							
							schemaInfos.addSchemaInfo(name, null);
						}
						
						return schemaInfos;
					}
				});
			} else {
				schemaInfos = this.connUtil.extractDatabaseMetaData(new DatabaseMetaDataCallback<SchemaInfos>() {
					@Override
					public SchemaInfos processMetaData(DatabaseMetaData dbmd) throws SQLException {
						SchemaInfos schemaInfos = new SchemaInfos();
						
						ResultSet rs = dbmd.getSchemas();
						
						while (rs.next()) {
							String name = rs.getString("TABLE_SCHEM");
							String catalog = rs.getString("TABLE_CATALOG");
							
							schemaInfos.addSchemaInfo(name, catalog);
						}
						
						return schemaInfos;
					}
				});
			}

			return schemaInfos;
		}

		@Override
		public List<String> getTableNames() {
			List<String> tableNames = this.connUtil.extractDatabaseMetaData(new DatabaseMetaDataCallback<List<String>>() {
				@Override
				public List<String> processMetaData(DatabaseMetaData dbmd) throws SQLException {
					// TODO: Consider Schema names for other DBs; refactor
					String schemaName = null;
					if (connUtil.getDbType() == DbType.ORACLE) {
						schemaName = getUserName();
					}
					
					ResultSet rs = dbmd.getTables(schemaName, schemaName, null, new String[]{"TABLE"});
					
					List<String> tableNames = new ArrayList<>();
					while (rs.next()) {
						String tableName = rs.getString("TABLE_NAME");
						// We check if the TableName already exists in the list as
						// Oracle seems to return the same table names multiple times on some
						// Schemas...
						if (!tableNames.contains(tableName)) {
							tableNames.add(tableName);
						}
					}
					rs.close();
					
					Collections.sort(tableNames);
					
					return tableNames;
				}
			});
			
			return tableNames;		
		}
		
		private static class SchemaInfoImpl implements SchemaInfo {
			private final String name;
			private final String catalog;
			
			private SchemaInfoImpl(String name, String catalog) {
				this.name = name;
				this.catalog = catalog;
			}
			
			@Override
			public String getName() {
				return this.name;
			}

			@Override
			public String getCatalog() {
				return this.catalog;
			}
		}
	}
	
	private static class JdbcConnUtil extends AbstractJuConnUtil {
		private final String url;
		private final String user;
		private final String password;
		
		private JdbcConnUtil(String url, String user, String password) {
			this.url = url;
			this.user = user;
			this.password = password;
		}
		
		@Override
		public void performDbWork(DbWork work) throws SQLException {
			try (Connection conn = DriverManager.getConnection(this.url, this.user, this.password)) {
				work.execute(conn);
			}
		}
	}
}
