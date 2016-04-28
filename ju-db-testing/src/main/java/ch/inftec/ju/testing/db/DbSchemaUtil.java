package ch.inftec.ju.testing.db;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.DbWork;
import ch.inftec.ju.db.DsWork;
import ch.inftec.ju.db.JuConnUtil;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.testing.db.DbDataUtil.ImportBuilder;
import ch.inftec.ju.util.AssertUtil;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuRuntimeException;

import com.googlecode.flyway.core.Flyway;

/**
 * Util class containing methods to perform DB Schema actions. Uses an EntityManager
 * to establish the connection to the corresponding DB.
 * @author Martin
 *
 */
public class DbSchemaUtil {
	private final Logger logger = LoggerFactory.getLogger(DbSchemaUtil.class);
	
	/**
	 * Optional utility instance to run liquibase updates. May be used if transaction handling requires it. 
	 */
	private final JuConnUtil connUtil;
	
	public DbSchemaUtil(JuConnUtil connUtil) {
		this.connUtil = connUtil;
	}
	
	public DbSchemaUtil(JuEmUtil emUtil) {
		this(emUtil.asConnUtil());
	}
	
	/**
	 * Creating a SchemaUtil with a JuEmUtil to get Database meta info and a JuConnUtil to perform
	 * the update or cleaning of the Schema.
	 * <p>
	 * JuEmUtil must be within a valid transaction - no explicit transaction handling will be done.
	 * @param emUtil
	 * @param connUtil
	 */
//	public DbSchemaUtil(JuEmUtil emUtil, JuConnUtil2 connUtil) {
//		this(emUtil, TxHandler.getDummyHandler(), connUtil);
//	}
//	
	public DbSchemaUtil(EntityManager em) {
		this(new JuEmUtil(em));
	}
	
	/**
	 * Initializes the DbSchemaUtil with an EntityManager and a UserTransaction object.
	 * <p>
	 * This initialization must be used in a container environment in a bean
	 * managed transaction context. Otherwise, we cannot control the transactions the way
	 * we have to. Liquibase uses its own transaction management and this is not possible if there
	 * is a managed transaction running. On the other hand, we perform some operations on the
	 * EntityManager that require a transaction to be active.
	 * @param em EntityManager provided by the container
	 * @param tx UserTransaction instance of a bean managed transaction context
	 */
	@Deprecated
	public DbSchemaUtil(EntityManager em, UserTransaction tx) {
		this(new JuEmUtil(em));
	}
	
	/**
	 * Runs the specified liquibase change log.
	 * <p>
	 * Liquibase uses a JDBC datasource to get a connection to the database and has its own transaction handling.
	 * Therefore, it won't participate in the current transaction.
	 * <p>
	 * We'll still require a transaction to be present for the current EntityManager. We'll commit that transaction
	 * in order to execute the Liquibase changes and begin a new transaction before returning.
	 * That means that the EntityManager's transaction will be open at the end of this method and can be used
	 * to perform further queries / updates.
	 * @param changeLogResourceName Name of the change log resource. The resource will be loaded using the
	 * default class loader.
	 */
	public void runLiquibaseChangeLog(final String changeLogResourceName) {
		this.runLiquibaseChangeLog(changeLogResourceName, null);
	}

	/**
	 * Method to actually run the Liquibase ChangeLog.
	 * <p>
	 * TODO:
	 * 
	 * @param changeLogResourceName
	 * @param parameters
	 */
	private void runLiquibaseChangeLog(final String changeLogResourceName, final Map<String, String> parameters) {
		// Make sure we have a transaction when accessing entity manager meta data
		final DbType dbType = this.connUtil.getDbType();
		final String metaDataUserName = this.connUtil.getMetaDataInfo().getUserName();
		
		this.connUtil.doWork(new DbWork() {
			@Override
			public void execute(Connection conn) {
				try {
					JdbcConnection jdbcConn = new JdbcConnection(conn);
					
					/*
					 * The default implementation of Liquibase for Oracle has an error in the default Schema
					 * lookup, so we'll set it here to avoid problems.
					 */
					Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConn);
					if (dbType == DbType.ORACLE) {
						db.setDefaultSchemaName(metaDataUserName);
					}
					
					/*
					 * Derby (and others) don't support the CREATE OR REPLACE syntax for Views and Liquibase will throw
					 * an error if the attribute is specified for Derby or H2.
					 * As we will use those DBs usually in memory, we'll just remote the attribute in all change logs
					 * using a custom ResourceAccessor that will filter the character stream.
					 */
					ResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor();
					if (dbType == DbType.DERBY || dbType == DbType.H2 || dbType == DbType.HSQL) {
						resourceAccessor = new ResourceAccessorFilter(resourceAccessor);
					}
					
					Liquibase liquibase = new Liquibase(changeLogResourceName, resourceAccessor, db);

					// Set parameters (if any)
					if (parameters != null) {
						for (String key : parameters.keySet()) {
							liquibase.setChangeLogParameter(key, parameters.get(key));
						}
					}

					// Run update
					liquibase.update((String)null);
				} catch (Exception ex) {
					throw new JuRuntimeException("Couldn't run Liquibase Update %s", ex, changeLogResourceName);
				}
			}
		});
	}
	
	/**
	 * Returns a builder to set up a Liquibase ChangeLog run.
	 * <p>
	 * Allows to set liquibase parameters as well.
	 * 
	 * @return
	 */
	public LiquibaseChangeLogBuilder liquibaseChangeLog() {
		return new LiquibaseChangeLogBuilder();
	}

	/**
	 * Helper class to run Liquibase ChangeLogs in a fluent API way.
	 * 
	 * @author martin.meyer@inftec.ch
	 * 
	 */
	public class LiquibaseChangeLogBuilder {
		private String resourcePath;
		private final Map<String, String> parameters = new HashMap<>();

		/**
		 * Sets the Liquibase ChangeLog resource path.
		 * 
		 * @param resourcePath
		 * @return
		 */
		public LiquibaseChangeLogBuilder changeLogResource(String resourcePath) {
			AssertUtil.assertNull("Multiple resources not supported yet.", this.resourcePath);
			this.resourcePath = resourcePath;
			return this;
		}

		/**
		 * Sets a key/value pair for Liquibase parameter substitution (parameters like ${key} in
		 * changeLogs will be replaced by the corresponding value.
		 * 
		 * @param name
		 *            Parameter name
		 * @param value
		 *            Substitution value
		 * @return
		 */
		public LiquibaseChangeLogBuilder parameter(String name, String value) {
			this.parameters.put(name, value);
			return this;
		}

		public void run() {
			runLiquibaseChangeLog(resourcePath, parameters);
		}
	}

	/**
	 * Runs Flyway migration scripts.
	 * @param locations Locations containing scripts in Flyway structure (e.g. db/migration).
	 */
	public void runFlywayMigration(final String... locations) {
		this.connUtil.doWork(new DsWork() {
			@Override
			public void execute(DataSource ds) {
				Flyway flyway = new Flyway();
				flyway.setDataSource(ds);
				flyway.setLocations(locations);
				flyway.migrate();
			}
		});
	}
	
	/**
	 * Clears the DB Schema.
	 * <p>
	 * Uses Flyway functionality.
	 */
	public void clearSchema() {
		this.connUtil.doWork(new DsWork() {
			@Override
			public void execute(DataSource ds) {
				Flyway flyway = new Flyway();
				flyway.setDataSource(ds);
				flyway.clean(); // FIXME: Try Liquibase.dropAll()
			}
		});
	}
	
	/**
	 * Creates the Default test DB Schema (Player, Team, TestingEntity...) and
	 * loads the default test data.
	 * <p>
	 * Also resets the sequences to 1.
	 */
	public void prepareDefaultSchemaAndTestData() {
		this.prepareDefaultTestData(false, true, true);
	}
	
	/**
	 * Loads the default test data and resets the sequences to 1.
	 * <p>
	 * Doesn't perform Schema updates.
	 */
	public void loadDefaultTestData() {
		this.prepareDefaultTestData(false, true, false);
	}
	
	/**
	 * Loads the default test data (Player, Team, TestingEntity, ...), making
	 * sure that the tables have been created using Liquibase.
	 * <p>
	 * This method will use Liquibase to perform Schema updates. Therefore, we'll need a transaction when the method is
	 * started that we will have to commit in order to execute Liquibase. We'll make sure however that we start a new
	 * transaction before returning.
	 * @param emptyTables If true, the default tables will be cleaned
	 * @param resetSequences If true, sequences (or identity columns) will be reset to 1
	 * @param createSchema If true, the Schema will be created (or verified) using Liquibase
	 */
	public void prepareDefaultTestData(boolean emptyTables, boolean resetSequences, boolean createSchema) {
		DbType dbType = this.connUtil.getDbType();
		
		if (createSchema) {
			this.runLiquibaseChangeLog("ju-testing/data/default-changeLog.xml");
			
			// For non-MySQL DBs, we also need to create the hibernate_sequence sequence...
			if (dbType != DbType.MYSQL) {
				this.runLiquibaseChangeLog("ju-testing/data/default-changeLog-hibernateSequence.xml");
			}
			
			// For MySQL DBs, we need to change the engine to support transactions, otherwise transaction tests
			// will fail
			if (dbType == DbType.MYSQL) {
				this.runLiquibaseChangeLog("ju-testing/data/default-changeLog-mySqlEngine.xml");
			}
		}
		
		DbDataUtil du = new DbDataUtil(this.connUtil);
		ImportBuilder fullData = du.buildImport().from("/ju-testing/data/default-fullData.xml");
		if (emptyTables) {
			fullData.executeDeleteAll();
		} else {
			fullData.executeCleanInsert();
		}
		
		// Load TIMEFIELD for non-oracle DBs
		if (this.connUtil.getDbType() != DbType.ORACLE && !emptyTables) {
			du.buildImport().from("/ju-testing/data/default-fullData-dataTypes.xml").executeUpdate();
		}
		
		if (resetSequences) {
			this.connUtil.getDbHandler().resetIdentityGenerationOrSequences(1);
		}
	}
	
	private class ResourceAccessorFilter implements ResourceAccessor {
		private final ResourceAccessor accessor;
		
		private ResourceAccessorFilter(ResourceAccessor accessor) {
			this.accessor = accessor;
		}

		@Override
		public InputStream getResourceAsStream(String file) throws IOException {
			logger.debug("Removing replaceIfExists attribute for resource " + file);
			InputStream is = this.accessor.getResourceAsStream(file);
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			String text = IOUtil.toString(reader);
			String newText = text.replaceAll("replaceIfExists=\"true\"", "");
			
			return new BufferedInputStream(new ByteArrayInputStream(newText.getBytes()));
		}

		@Override
		public Enumeration<URL> getResources(String packageName)
				throws IOException {
			return this.accessor.getResources(packageName);
		}

		@Override
		public ClassLoader toClassLoader() {
			return this.accessor.toClassLoader();
		}
	}
}
