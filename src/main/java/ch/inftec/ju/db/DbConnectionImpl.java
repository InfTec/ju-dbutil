package ch.inftec.ju.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;

import ch.inftec.ju.db.DbRowUtils.DbRowsImpl;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.change.DbAction;
import ch.inftec.ju.util.change.DbActionUtils;


/**
 * Implementation of the DbConnection interface.
 * <p>
 * Expects a DataSource and JdbcTemplate to be autowired by Spring.
 * @author Martin
 *
 */
final class DbConnectionImpl implements DbConnection {
	Logger log = LoggerFactory.getLogger(DbConnectionImpl.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DataSource dataSource;
	
	private String name;
	private String schemaName;
	
//	/**
//	 * Creates a new connection creator with the specified name.
//	 * @param name Unique name of the connection
//	 * @param entityManagerFactory Factory used to created an EntityManager that will back
//	 * up the connection
//	 */
//	protected DbConnectionImpl(String name, String schemaName, EntityManagerFactory entityManagerFactory) {
//		this.name = name;
//		this.schemaName = schemaName;
//	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public String getSchemaName() {
		return this.schemaName;
	}

	@Override
	public List<String> getTableNames() throws JuDbException {
		DbMetaData md = new DbMetaData();
		return md.getTableNames();
	}
	
	@Override
	public String getPrimaryColumnName(String tableName) throws JuDbException {
		DbMetaData md = new DbMetaData();
		return md.getPrimaryColumnName(tableName);
	}

	@Override
	public List<String> getColumnNames(String tableName) throws JuDbException {
		DbMetaData md = new DbMetaData();
		return Arrays.asList(md.getColumnNames(tableName));			
	}
	
	@Override
	public DbQueryRunner getQueryRunner() {
		return new DbQueryRunnerImpl(this);
	}
	
	@Override
	public Connection getConnection() {
		return DataSourceUtils.getConnection(this.dataSource);
	}
	
	/**
	 * Helper class to wrap around a DatabaseMetaData object.
	 * Make sure to call the close() method when the object is not used any longer.
	 * @author Martin
	 *
	 */
	private class DbMetaData {
		private DatabaseMetaData metaData;
		private ResultSet rs;
		
		/**
		 * Creates a new DbMetaData instance. If an exception occurs, close will be called
		 * implicitly.
		 * @throws JuDbException If the MetaData cannot be accessed
		 */
		public DbMetaData() throws JuDbException {
			try {
				this.metaData = DbConnectionImpl.this.getConnection().getMetaData();
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't access DatabaseMetaData", ex);
			}
		}
		
		public List<String> getTableNames() throws JuDbException {
			try {
				this.rs = this.metaData.getTables(getSchemaName(), null, null, new String[]{"TABLE"});
				
				List<String> tableNames = new ArrayList<>();
				while (rs.next()) {
					String tableName = rs.getString("TABLE_NAME");
					tableNames.add(tableName.toUpperCase());
				}
				
				Collections.sort(tableNames);
				
				return tableNames;
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate table names", ex);
			} finally {
				JuDbUtils.closeQuietly(this.rs);
				this.rs = null;
			}
		}
		
		/**
		 * Gets the primary column name for the specified table.
		 * @param tableName Table name
		 * @return Primary column name
		 * @throws JuDbException If the primary column name cannot be evaluated
		 */
		public String getPrimaryColumnName(String tableName) throws JuDbException {
			try {
				this.rs = this.metaData.getPrimaryKeys(null, null, tableName.toUpperCase());
				
				String columnName = null;
				if (rs.next()) columnName = rs.getString("COLUMN_NAME");
				else throw new JuDbException("Couldn't evaluate primary key for table " + tableName);
				
				if (rs.next()) {
					throw new JuDbException("Driver returned multiple primary keys for table " + tableName);
				}
				
				return columnName.toUpperCase();
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate primary key for table " + tableName, ex);
			} finally {
				JuDbUtils.closeQuietly(this.rs);
				this.rs = null;
			}
		}
		
		/**
		 * Gets all column names of the specified table in the order they are defined, or rather in the
		 * order the driver returns them.
		 * @param tableName Table name
		 * @return Column names of the table
		 * @throws JuDbException If the column names cannot be evaluated
		 */
		public String[] getColumnNames(String tableName) throws JuDbException {
			try {
				this.rs = this.metaData.getColumns(null, null, tableName.toUpperCase(), null);			
				
				ArrayList<String> columnNames = new ArrayList<String>();
				
				while (rs.next()) {
					String columnName = rs.getString("COLUMN_NAME");
					columnNames.add(columnName.toUpperCase());
				}
				
				if (columnNames.size() == 0) {
					throw new JuDbException("Couldn't evaluate column names for table " + tableName + ": Driver returned empty ResultSet.");
				}
				
				return (String[])columnNames.toArray(new String[0]);
			} catch (JuDbException ex) {
				throw ex;
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't evaluate primary key for table " + tableName, ex);
			} finally {
				JuDbUtils.closeQuietly(this.rs);
				this.rs = null;
			}
		}
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
	
	/**
	 * Implementation of the DbQueryRunner interface that works with DbConnection
	 * instances.
	 * @author TGDMEMAE
	 *
	 */
	private final class DbQueryRunnerImpl implements DbQueryRunner {
		private DbConnectionImpl dbConnection;
		
		/**
		 * Creates a new DbQueryRunner using the specified DbConnection.
		 * @param dbConnection DbConnection instance
		 */
		public DbQueryRunnerImpl(DbConnectionImpl dbConnection) {
			this.dbConnection = dbConnection;
		}
		
		/**
		 * Gets a connection to the database.
		 * @return Connection instance
		 * @throws JuDbException If the connection cannot be established
		 */
		private Connection getConnection() throws JuDbException {
			return this.dbConnection.getConnection();
		}
			
		@Override
		public DbRowsImpl query(String query, Object... params) throws JuDbException {
			try {
				QueryRunner qr = new QueryRunner();
				return qr.query(this.getConnection(), query, new DbRowResultSetHandler(), this.processParams(params));
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't execute query: " + query, ex);
			}
		}

		@Override
		public int update(String query, Object... params) throws JuDbException {
			try {
				QueryRunner qr = new QueryRunner();
				return qr.update(this.getConnection(), query, this.processParams(params));
			} catch (SQLException ex) {
				throw new JuDbException("Couldn't execute update: " + query, ex);
			}
		}
		
		@Override
		public DbRow primaryKeyQuery(String tableName, Object primaryKeyValue) throws JuDbException {
			String selectQry = "SELECT * FROM " + tableName + " WHERE " + this.dbConnection.getPrimaryColumnName(tableName) + "=?";

			DbRows dbRows = this.query(selectQry, primaryKeyValue);
			
			if (dbRows.getRowCount() > 1) {
				throw new JuDbException("PrimaryKeyQuery for " + tableName + " with key=" + primaryKeyValue 
						+ ". Expected exactly 1 row, but got " + dbRows.getRowCount());
			}
			
			return dbRows.getRowCount() == 0 ? null : dbRows.getRow(0);
		}
		
		/**
		 * Executes a select * query on the specified table that returns no rows. Can be used
		 * to obtain an empty DbRows instance.
		 * @param tableName Table name
		 * @return DbRows instance with no rows
		 * @throws JuDbException If the query fails
		 */
		@Override
		public DbRow emptyRowQuery(String tableName) throws JuDbException {
			String selectQry = "SELECT * FROM " + tableName + " WHERE 1=0";
			return this.query(selectQry).getBaseRow();
		}
		
		@Override
		public DbAction getUpdateAction(String tableName, Object primaryKeyValue) throws JuDbException {
			DbRow row = this.primaryKeyQuery(tableName, primaryKeyValue);
			return row == null ? null : DbActionUtils.newUpdateAction(this.dbConnection, row, tableName).getAction();
		}
		
		@Override
		public DbAction getInsertAction(String tableName) throws JuDbException {
			return DbActionUtils.newInsertAction(this.dbConnection, tableName).getAction();
		}
		
		@Override
		public DbAction getDeleteAction(String tableName, Object primaryKeyValue) throws JuDbException {
			return DbActionUtils.newDeleteAction(this.dbConnection, tableName, primaryKeyValue);
		}
		
		/**
		 * Processes the parameters that are send to the QueryRunner.
		 * <p>
		 * For instance, this will convert a java.util.Date to a java.sql.Date
		 * @param object Parameters array
		 * @return Array with converted parameters
		 */
		private Object[] processParams(Object[] params) {
			Object[] newParams = Arrays.copyOf(params, params.length);
			
			for (int i = 0; i < newParams.length; i++) {
				Object param = newParams[i];
				
				if (param != null) {
					if (param.getClass() == java.util.Date.class) {
						newParams[i] = new java.sql.Date(((java.util.Date)param).getTime());
					}
				}
			}
			
			return newParams;
		}
	}
}
