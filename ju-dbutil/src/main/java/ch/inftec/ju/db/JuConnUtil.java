package ch.inftec.ju.db;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;

import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;


/**
 * Interface providing helper functionality for (raw JDBC) DB connections.
 * <p>
 * Note that implementations will not handle DB transactions - they will just assume that
 * a transaction exists for the wrapped DB connection.
 * @author Martin
 *
 */
public interface JuConnUtil {
	/**
	 * Gets the type of the DB implementation of this EntityManager. If the type is not known (or supported)
	 * by JuEmUtil, an exception is thrown.
	 * @return DbType
	 */
	DbType getDbType();
	
	/**
	 * Executes some DB work using a raw JDBC connection.
	 * @param work DbWork callback interface
	 */
	void doWork(DbWork work);
	
	/**
	 * Executes some DB work using a DataSource.
	 * @param work DsWork callback interface
	 */
	void doWork(DsWork work);
	
	/**
	 * Gets Connection MetaData info
	 * @return MetaDataInfo instance
	 */
	MetaDataInfo getMetaDataInfo();
	
	/**
	 * Gets an instance of a DbHandler to perform DB specific tasks.
	 * @return DbHandler implementation
	 */
	DbHandler getDbHandler();
	
	/**
	 * MetaDataInfo holder
	 * @author Martin
	 *
	 */
	public static interface MetaDataInfo {
		/**
		 * Gets the UserName retrieved from the Connection MetaData
		 * @return Username
		 */
		String getUserName();
		
		/**
		 * Gets the URL retrieved from the Connection MetaData
		 * @return URL
		 */
		String getUrl();
		
		/**
		 * Gets a list of the primary key columns for the specified table
		 * <p>
		 * Column names are kept the way the driver returns them (may be upper, lower or mixed case)
		 * @param tableName Table name
		 * @return List of all columns that make up the primary key. If no primary key is applied, an empty list is returned.
		 */
		List<String> getPrimaryKeyColumns(String tableName);
		
		/**
		 * Gets a list of all table names of the DB. Table names are returned the way the DB driver
		 * returns them, which may be lower, mixed or upper case.
		 * @return List of Table names
		 */
		List<String> getTableNames();

		/**
		 * Gets the info to the Schema we are conneted to (or the default Schema).
		 * @return
		 */
		SchemaInfo getSchemaInfo();
		
		/**
		 * Gets a list of all Schemas in the DB, as returned by the DB driver.
		 * 
		 * @return Schemas as returned by the DB.
		 */
		SchemaInfos getSchemaInfos();

		public static final class SchemaInfos {
			private final List<SchemaInfo> schemas = new ArrayList<>();

			/**
			 * Adds the specified schema to the list of this info object.
			 * 
			 * @param name
			 *            Schema name
			 * @param catalog
			 *            Catalog or null if DB doesn't use catalogs
			 */
			void addSchemaInfo(final String name, final String catalog) {
				this.schemas.add(new SchemaInfo() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public String getCatalog() {
						return catalog;
					}
				});
			}

			/**
			 * Gets the names of all Schemas in the DB (without the catalog).
			 * 
			 * @return
			 */
			public Set<String> getSchemaNames() {
				Set<String> schemaNames = new HashSet<>();

				for (SchemaInfo info : this.schemas) {
					schemaNames.add(info.getName());
				}

				return schemaNames;
			}

			/**
			 * Gets all catalogs in which the specified schema is defined.
			 * <p>
			 * This will also include a null entry if the schema is defined without a catalog.
			 * 
			 * @param schemaName
			 *            Schema name
			 * @return
			 */
			public Set<String> getCatalogs(String schemaName) {
				Set<String> catalogs = new HashSet<>();

				for (SchemaInfo info : this.schemas) {
					if (info.getName().equals(schemaName)) {
						catalogs.add(info.getCatalog());
					}
				}

				return catalogs;
			}

			/**
			 * Gets a list of SchemaInfos for the specified schema name.
			 * 
			 * @param schemaName
			 *            Schema name
			 * @return
			 */
			public List<SchemaInfo> getSchemaInfos(String schemaName, String catalog) {
				List<SchemaInfo> schemaInfos = new ArrayList<>();

				for (SchemaInfo info : this.schemas) {
					if (info.getName().equals(schemaName) && ObjectUtils.equals(info.getCatalog(), catalog)) {
						schemaInfos.add(info);
					}
				}

				return schemaInfos;
			}
		}

		/**
		 * Schema info (consisting of name and (optional) catalog).
		 * 
		 * @author martin.meyer@inftec.ch
		 *
		 */
		public static interface SchemaInfo {
			/**
			 * Gets the name of the schema.
			 * 
			 * @return
			 */
			String getName();

			/**
			 * Gets the schema catalog (if any) or null if DB doesn't use catalogs.
			 * 
			 * @return
			 */
			String getCatalog();
		}
	}
	
	/**
	 * Helper interface to execute DB specific tasks.
	 * @author Martin
	 *
	 */
	public static interface DbHandler {
		/**
		 * Converts the casing of the specified tableName so the DB will understand it.
		 * @param tableName Table name
		 * @return Table name in casing the DB will understand
		 */
		String convertTableNameCasing(String tableName);
		
		/**
		 * Gets a list of all sequence names of the DB, as returned by the driver.
		 * @return List of sequence names
		 */
		List<String> getSequenceNames();
		
		/**
		 * Gets the next value from the specified sequence.
		 * @param sequenceName Sequence name
		 * @return Next value for the sequence
		 */
		Long getNextValueFromSequence(String sequenceName);
		
		/**
		 * Resets identity generation of all tables or sequences to allow for predictable
		 * and repeatable entity generation.
		 * @param val Value for the next primary key
		 */
		void resetIdentityGenerationOrSequences(int val);
		
		/**
		 * Wraps the specified expression in a DB specific to_lower_case statement.
		 * @param expression Expression
		 * @return Expression wrapped in a DB specific to_lower_case statement
		 */
		String wrapInLowerString(String expression);

		/**
		 * Builder to create a new DB schema.
		 * 
		 * @return DbSchemaBuilder to create a new DB Schema or user.
		 */
		DbSchemaBuilder createSchema();

		/**
		 * Drops the specified schema.
		 * 
		 * @param schemaInfo
		 *            Schema info identifying the schema by name and optional catalog.
		 * @param users Optional list of users to drop as well
		 */
		void dropSchema(SchemaInfo schemaInfo, String... users);
	}
	
	/**
	 * Method to extract info from the DatabaseMetaData.
	 * @param action Callback method to do the data extracting into an arbitrary data structure.
	 * @return Data as returned by the callback function
	 */
	public <T> T extractDatabaseMetaData(final DatabaseMetaDataCallback<T> action);
	
	public enum DbType {
		DERBY {
			@Override
			protected DbHandler getDbSpecificHandler(JuConnUtil connUtil) {
				return new DbSpecificHandlerDerby(connUtil);
			}
		},
		H2 {
			@Override
			protected DbHandler getDbSpecificHandler(JuConnUtil connUtil) {
				return new DbSpecificHandlerH2(connUtil);
			}
		},
		HSQL {
			@Override
			protected DbHandler getDbSpecificHandler(JuConnUtil connUtil) {
				return new DbSpecificHandlerHsql(connUtil);
			}
		},
		MYSQL {
			@Override
			protected DbHandler getDbSpecificHandler(JuConnUtil connUtil) {
				return new DbSpecificHandlerMySql(connUtil);
			}
		},
		ORACLE {
			@Override
			protected DbHandler getDbSpecificHandler(JuConnUtil connUtil) {
				return new DbSpecificHandlerOracle(connUtil);
			}
		};
		
		static DbType evaluateDbType(String productName) {
			if (productName.toLowerCase().contains("derby")) {
				return DERBY;
			} else if (productName.toLowerCase().contains("h2")) {
				return H2; 
			} else if (productName.toLowerCase().contains("hsql")) {
				return HSQL;
			} else if (productName.toLowerCase().contains("mysql")) {
				return MYSQL;
			} else if (productName.toLowerCase().contains("oracle")) {
				return ORACLE;
			} else {
				throw new JuDbException("Unknown DB. Product name: " + productName);
			}
		}
		
		protected abstract DbHandler getDbSpecificHandler(JuConnUtil connUtil);
	}
}
