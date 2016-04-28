package ch.inftec.ju.testing.db;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuConnUtil;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;
import ch.inftec.ju.db.JuConnUtils;
import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.util.JuUtils;
import ch.inftec.ju.util.TestUtils;

/**
 * Base class for DB tests.
 * <p>
 * Provides a machanism to evaluate the test DB at runtime, thus enabling DB tests
 * targeting various DB implementations.
 * @author Martin
 *
 */
public class AbstractDbTest {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected JuConnUtil connUtil;
	
	protected EntityManager em;
	protected JuEmUtil emUtil;
	
	private JuEmfUtil emfUtil;
	private EmfWork emfWork;
	
	/**
	 * Helper method that allows us to prepare a Schema as defined in the DB test profile.
	 * <p>
	 * This method can be called multiple time, it will only create the Schema the first time.
	 * <p>
	 * TODO: Whole Schema handling should be refactored...
	 */
	public static void prepareSchemaByProfile() {
		new DbInitializerRule(null).prepareSchemaByProfile();
	}
	
	/**
	 * Rule to initialize DB fields. We need to use a rule so we can evaluate the method
	 * annotation.
	 */
	@Rule
	public DbInitializerRule dbInitializer = new DbInitializerRule(this);
	
	@After
	public void cleanupDb() {
		if (this.emfWork != null) {
			this.emfWork.close();
			this.emfWork = null;
			this.em = null;
		}
	}

	/**
	 * Sets the transaction to rollback.
	 */
	protected final void setRollbackOnly() {
		this.emfWork.setRollbackOnly();
	}
	
	/**
	 * Starts a new EmfWork. The caller is responsible to close the work.
	 * @return EmfWork with a new transaction
	 */
	protected final EmfWork startNewWork() {
		return this.emfUtil.startWork();
	}
	
	/**
	 * This method can be overridden by extending classes to run DB initialization scripts before the test is
	 * actually run.
	 * <p>
	 * The method is self-responsible to release any resources aquired from the provided JuEmtUtil instance.
	 * <p>
	 * The default implementation is empty.
	 * @param emfUtil JuEmfUtil instance that can be used to access the DB
	 */
	protected void runDbInitializationScripts(JuEmfUtil emfUtil) {
	}
	
	private static class DbInitializerRule implements TestRule {
		private Logger logger = LoggerFactory.getLogger(DbInitializerRule.class);

		private static final Set<String> createdSchemas = new HashSet<>();

		private final AbstractDbTest dbTest;
		
		DbInitializerRule(AbstractDbTest dbTest) {
			this.dbTest = dbTest;
		}
		
		private void prepareSchemaByProfile() {
			String profile = JuUtils.getJuPropertyChain().get("ju-dbutil-test.profile", true);

			// Check if we have an admin profile set as well. In this case, we'll support creating
			// the user/schema if it's missing.
			String adminPassword = JuUtils.getJuPropertyChain().get(String.format("ju-dbutil-test.%sAdmin.password", profile), false);
			String schemaName = JuUtils.getJuPropertyChain().get(String.format("ju-dbutil-test.%s.schema", profile), false);
			String userName = JuUtils.getJuPropertyChain().get(String.format("ju-dbutil-test.%s.user", profile), false);
			if (StringUtils.isEmpty(schemaName)) {
				schemaName = userName;
			}

			if (!createdSchemas.contains(schemaName) && !StringUtils.isEmpty(adminPassword)) {
				String adminProfile = profile + "Admin";
				logger.info("Admin-Profile defined: {}. Creating Schema '{}' if necessary", adminProfile, schemaName);
				JuConnUtil adminConnUtil = JuConnUtils.build()
						.profile(adminProfile)
						.create();

				// Check if the Schema exists and if we should delete it if so
				boolean createSchema = true;
				if (adminConnUtil.getMetaDataInfo().getSchemaInfos().getSchemaNames().contains(schemaName)) {
					boolean dropExistingSchema = JuUtils.getJuPropertyChain().get(
							String.format("ju-dbutil-test.%s.dropExistingSchema", adminProfile), Boolean.class, "false");

					if (dropExistingSchema) {
						logger.info("Schema {} already exists. Dropping and recreating.", schemaName);
						List<SchemaInfo> schemaInfos = adminConnUtil.getMetaDataInfo().getSchemaInfos()
								.getSchemaInfos(schemaName, null);
						Assert.assertEquals("Catalogs not supported yet", 1, schemaInfos.size());

						if (adminConnUtil.getDbType() == DbType.MYSQL) {
							// For MySQL, drop the user as well
							adminConnUtil.getDbHandler().dropSchema(schemaInfos.get(0), userName);
						} else {
							adminConnUtil.getDbHandler().dropSchema(schemaInfos.get(0));
						}
					} else {
						createSchema = false;
						logger.info(
								"Schema {} already exists. Skipping creation. Set dropExistingSchema for adminProfile to true if Schema should be dropped and recreated.",
								schemaName);
					}
				}

				if (createSchema) {
					logger.info("Creating Schema {}", schemaName);

					String schemaPassword = JuUtils.getJuPropertyChain().get(String.format("ju-dbutil-test.%s.password", profile), false);
					boolean jtaRecoveryGrants = JuUtils.getJuPropertyChain().get(
							String.format("ju-dbutil-test.%s.jtaRecoveryGrants", profile), Boolean.class, "false");
					
					adminConnUtil.getDbHandler().createSchema()
							.name(schemaName)
							.user(userName) // Ignored for DBs that don't have users
							.password(schemaPassword)
							.jtaRecoveryGrants(jtaRecoveryGrants)
							.create();

					createdSchemas.add(schemaName);
				}
			}
		}
		
		@Override
		public Statement apply(final Statement base, final Description description) {
			final Method method = TestUtils.getTestMethod(description);
			
			// Evaluate Persistence Unit name
			String persistenceUnit = "ju-pu-test";
			String profile = null;
			
			// Check if the persistenceUnit is overwritten by an annotation (method overrules
			// class annotation)
			JuDbTest juDbTest = method.getAnnotation(JuDbTest.class);
			if (juDbTest == null) {
				juDbTest = this.dbTest.getClass().getAnnotation(JuDbTest.class);
			}
			if (juDbTest != null) {
				persistenceUnit = juDbTest.persistenceUnit();
				profile = juDbTest.profile();
			}
			
			if (StringUtils.isEmpty(profile)) {
				profile = JuUtils.getJuPropertyChain().get("ju-dbutil-test.profile", true);

				this.prepareSchemaByProfile();
			}
			
			this.dbTest.connUtil = JuConnUtils.build()
				.profile(profile)
				.create();
			
			this.dbTest.emfUtil = new EmfUtilProvider().createEmfUtil(persistenceUnit, profile);
						
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					// Run dbInitializationScripts
					dbTest.runDbInitializationScripts(dbTest.emfUtil);
					
					DbTestAnnotationHandler annotationHandler = new DbTestAnnotationHandler(method, description);
					
					// Run preAnnotations in own transaction
					try (EmfWork ew = dbTest.emfUtil.startWork()) {
						annotationHandler.executePreTestAnnotations(ew.getEmUtil());
					}
					
					// Initialize protected fields of test class
					dbTest.emfWork = dbTest.emfUtil.startWork();
					dbTest.em = dbTest.emfWork.getEm();
					dbTest.emUtil = new JuEmUtil(dbTest.em);

					// Run test method
					base.evaluate();
					
					// Run post server code in own transaction
					try (EmfWork ew = dbTest.emfUtil.startWork()) {
						annotationHandler.executePostServerCode(ew.getEmUtil());
					}
					
					// Run postAnnotations in own transaction
					try (EmfWork ew = dbTest.emfUtil.startWork()) {
						annotationHandler.executePostTestAnnotations(ew.getEmUtil());
					}
				}
			};			
		}
	}
}