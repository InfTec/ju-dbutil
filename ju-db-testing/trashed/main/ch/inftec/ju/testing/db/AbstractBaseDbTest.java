package ch.inftec.ju.testing.db;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.db.DbRow;
import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.testing.db.data.TestDb;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.TestUtils;
import ch.inftec.ju.util.comparison.ValueComparator;

/**
 * Base class for tests that use test database data.
 * <p>
 * Extending classes must be annotated with @ContextConfiguration and provide an
 * appropriated Spring context.
 *
 * @author tgdmemae
 * @deprecated Use AbstractDbTest instead
 *
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
//    DirtiesContextTestExecutionListener.class,
//    DbInitializerTestExecutionListener.class,
//    TransactionalTestExecutionListener.class,    
//    TransactionDbUnitTestExecutionListener.class })
@Deprecated
public abstract class AbstractBaseDbTest {
	/**
	 * Helper class to initialize the DB. JUnit @Before is not sufficient as this must run
	 * before the TransactionDbUnitTestExecutionListener runs...
	 * @author Martin
	 *
	 */
	public static class DbInitializerTestExecutionListener extends AbstractTestExecutionListener {
		
		@Override
		public void beforeTestMethod(TestContext testContext) throws Exception {
			testContext.getTestClass().getMethod("resetDatabase").invoke(testContext.getTestInstance());
		}
	}
	
	protected final Logger log = LoggerFactory.getLogger(this.getClass());
	
//	/**
//	 * Extending classes can use this DbConnection instance in their test methods. It will be
//	 * automatically set at the beginning of each test case and closed after each test case.
//	 */
//	protected DbConnection dbConn;
	
	/**
	 * EntityManager instance to the Test DB.
	 */
	@PersistenceContext
	protected EntityManager em;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private ConnectionInfo connectionInfo;
	
	@Autowired
	protected JdbcTemplate jdbcTemplate;
		
	@Autowired(required=false)
	private TestDb testDb;
	
	@Autowired(required=false)
	private DefaultDataTypeFactory dataTypeFactor;
	
	@Autowired
	private JuDbUtils juDbUtils;
	
	/**
	 * Gets an instance of a DbDataUtil for the current connection.
	 * @return DbDataUtil instance
	 */
	protected final DbDataUtil createDbDataUtil() {
		DbDataUtil util = new DbDataUtil(this.em);
//		util.setSchema(this.connectionInfo.getSchema());
		if (this.dataTypeFactor != null) {
			util.setConfigProperty("http://www.dbunit.org/properties/datatypeFactory", this.dataTypeFactor);
		}
		
		return util;
	}
	
//	/**
//	 * Helper method to load the data of the provided DefaultDataSet.
//	 * <p>
//	 * This will perform a clean insert of the data contained in the set, but
//	 * leave tables not included in the set unaffected.
//	 * @param dataSet DataSet to load data of
//	 */
//	protected final void loadDataSet(DefaultDataSet dataSet) {
//		this.loadDataSet(dataSet.getUrl());
//	}
	
//	/**
//	 * Helper method to load the data of the provided import file.
//	 * <p>
//	 * This will perform a clean insert of the data contained in the set, but
//	 * leave tables not included in the set unaffected.
//	 * @param testDataFile URL to a test data file
//	 */
//	protected final void loadDataSet(URL testDataFile) {
//		this.testDb.loadTestData(testDataFile);
//	}
//	
//	/**
//	 * Helper method to load the data from the provided import file.
//	 * @param testDataFile Path to a test data file on the classpath
//	 */
//	protected final void loadDataSet(String testDataFile) {
//		this.loadDataSet(IOUtil.getResourceURL(testDataFile));
//	}
	
	@Before
	public final void resetDatabase() throws Exception {
		this.em.getMetamodel();
		
		if (this.testDb != null) {
			this.testDb.resetDatabase();
		}
	}
	
	/**
	 * This method can be overridden by extending classes if they need
	 * to perform DB initialization code before the transaction and DbUnit
	 * code is executed (e.g. to set up the Database.
	 * <p>
	 * This method will be called once for a connection.
	 * <p>
	 * The default JPA default tables are already created prior to this method.
	 */
	protected void doInitDatabase() {
	}
	
//	
//	@After
//	public final void closeConnection() throws Exception {
//		if (this.dbConn != null) this.dbConn.close();
//	}
	
	/**
	 * Reinitializes the connection (i.e. dbConn, em and qr) of the test case.
	 * <p>
	 * This will implicitly commit all transactions and can be done to make sure changed
	 * data can be seen by other transactions.
	 * @param evictCache If true, the EntityManager cache will be evicted. Use this if data
	 * has been modified outside the EntityManager.
	 */
	protected final void reInitConnection(boolean evictCache) {
		this.em.clear();
		if (evictCache) this.em.getEntityManagerFactory().getCache().evictAll();
//		try {
//			this.closeConnection();
//			this.dbConn = this.openDbConnection();
//			this.em = this.dbConn.getEntityManager();
//			this.qr = this.dbConn.getQueryRunner();
//			
//			if (evictCache) this.em.getEntityManagerFactory().getCache().evictAll();
//			
//			this.doReInitConnection();
//		} catch (Exception ex) {
//			throw new JuDbException("Couldn't reinit connection", ex);
//		}
	}
	
	/**
	 * Extending classes can override this method to perform custom reinitialization.
	 */
	protected void doReInitConnection() {		
	}
	
	/**
	 * Asserts that the values of a row map those of the specified map. Uses
	 * a ValueComparator to compare the values, thus making sure that the tests
	 * succeed if for instance some database implementations return Long instances and some
	 * Integer.
	 * @param row DbRow instance
	 * @param expectedValues Expected values in a map, having the column names as keys
	 */
	protected final void assertRowEquals(DbRow row, Map<String, Object> expectedValues) {
		HashMap<String, Object> rowValues = new HashMap<String, Object>();
		
		for (int i = 0; i < row.getColumnCount(); i++) {
			rowValues.put(row.getColumnName(i), row.getValue(row.getColumnName(i)));
		}
		
		TestUtils.assertMapEquals(expectedValues, rowValues, ValueComparator.INSTANCE);
	}
	
	/**
	 * Asserts that the values of a row map those of specified keyValuePairs.
	 * @param row DbRow instance
	 * @param keyValuePairs KeyValue pairs
	 */
	protected final void assertRowEquals(DbRow row, Object... keyValuePairs) {
		this.assertRowEquals(row, JuCollectionUtils.stringMap(keyValuePairs));
	}
}
