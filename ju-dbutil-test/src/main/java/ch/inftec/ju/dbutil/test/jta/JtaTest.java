package ch.inftec.ju.dbutil.test.jta;

import javax.persistence.PersistenceException;
import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import bitronix.tm.resource.ResourceRegistrar;
import ch.inftec.ju.db.JuConnUtil;
import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuConnUtils;
import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.testing.db.JuAssumeUtils;
import ch.inftec.ju.util.JuUtils;

public class JtaTest {
	private ConfigurableApplicationContext ctx;
	private JtaTestBean jtaTestBean;
	
	@BeforeClass
	public static void prepareSchema() {
		boolean skipJtaTests = JuUtils.getJuPropertyChain().get("ju-dbutil-test.jtaTests.skip", Boolean.class, "false");
		Assume.assumeFalse("Skipping JTA tests", skipJtaTests);

		AbstractDbTest.prepareSchemaByProfile();
	}

	@Before
	public void prepareContext() {
		// We need to unregister all resources, otherwise Bitronix will complain...
		for (String resName : ResourceRegistrar.getResourcesUniqueNames()) {
			ResourceRegistrar.unregister(ResourceRegistrar.get(resName));
		}
		
		String dbContextFile = JuUtils.getJuPropertyChain().get("ju-dbutil-test.jta.contextFile");

		this.ctx = new ClassPathXmlApplicationContext(new String[] { "JtaTest-context.xml", dbContextFile }, JtaTest.class);
		
		this.jtaTestBean = this.ctx.getBean(JtaTestBean.class);
		
		// Run liquibase update and create default data
		// Note that we need an unmanaged DataSource to do so
		DataSource ds1 = ctx.getBean("jpaDb1DataSource", DataSource.class);
		new DbSchemaUtil(JuConnUtils.createByDataSource(ds1)).prepareDefaultSchemaAndTestData();
		
		DataSource ds2 = ctx.getBean("jpaDb2DataSource", DataSource.class);
		new DbSchemaUtil(JuConnUtils.createByDataSource(ds2)).prepareDefaultSchemaAndTestData();
	}
	
	@After
	public void releaseContext() {
		this.ctx.close();
	}
	
	@Test
	public void jpa_canReadData_withoutTransaction() {
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb1(-1L));
	}
	
	@Test
	public void jpa_doesNotWrite_withoutTransaction() {
		this.jtaTestBean.setNameNoTransJpaDb1(-1L, "newName");
		
		// Check with both entity managers
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb1(-1L));
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb12(-1L));
	}
	
	@Test
	public void jpa_canWriteData_withTransaction() {
		this.jtaTestBean.setNameTransJpaDb1(-1L, "newName", false);
		
		// Check with both entity managers
		Assert.assertEquals("newName", this.jtaTestBean.getNameNoTransJpaDb1(-1L));
		Assert.assertEquals("newName", this.jtaTestBean.getNameNoTransJpaDb12(-1L));
	}
	
	@Test
	public void jpa_canRollbackData_withTransaction() {
		this.setNameWithRollbackJpa1("newName");
		
		// Check with both entity managers
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb1(-1L));
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb12(-1L));
	}
	
	/**
	 * Transaction is not available when using two EntityManagers at the same time
	 * -> No data is written...
	 */
	@Test
	public void jpa_cannotWriteData_withTransaction_toJustOneDb_usingTwoEntityManagers() {
		this.jtaTestBean.setNameTransJpaDb1and2(-1L, "newName1", -1L, "newName2", false);
		
		// Check with both entity managers
		Assert.assertEquals("newName1", this.jtaTestBean.getNameNoTransJpaDb1(-1L));
		
		// DB2 won't be written as we don't have a transaction for it
		this.assumeTwoDbs(); // Skip for physical DBs where we only use one Schema
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb2(-1L));
	}
	
	@Test
	public void jpa_canRollbackData_withTransaction_usingTwoEntityManagers() {
		this.setNameWithRollbackJpaBoth("newName1", "newName2");
		
		// Check with both entity managers
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb1(-1L));
		
		// Wouldn't have been written anyway...
		Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJpaDb2(-1L));
	}
	
	private void setNameWithRollbackJpa1(String name) {
		try {
			this.jtaTestBean.setNameTransJpaDb1(-1L, name, true);
			Assert.fail("No Exception");
		} catch (RuntimeException ex) {
			// Expected
		}
	}
	
	private void setNameWithRollbackJpaBoth(String name1, String name2) {
		try {
			this.jtaTestBean.setNameTransJpaDb1and2(-1L, name1, 1L, name2, true);
			Assert.fail("No Exception");
		} catch (RuntimeException ex) {
			// Expected
		}
	}
	
	@Test(expected=PersistenceException.class)
	public void jta_cannotReadData_withoutTransaction() {
		Assert.assertEquals("Test1x", this.jtaTestBean.getNameNoTransJtaDb1(-1L));
	}
	
	@Test
	public void jta_canReadData_withTransaction() {
		Assert.assertEquals("Test1", this.jtaTestBean.getNameTransJtaDb1(-1L));
	}
	
	@Test
	public void jta_canWriteData_withTransaction() {
		this.jtaTestBean.setNameTransJtaDb1(-1L, "newName", false);
		
		// Check with both entity managers
		Assert.assertEquals("newName", this.jtaTestBean.getNameTransJtaDb1(-1L));
		Assert.assertEquals("newName", this.jtaTestBean.getNameTransJtaDb12(-1L));
	}
	
	@Test
	public void jta_canRollbackData_withTransaction() {
		this.setNameWithRollbackJta1("newName");
		
		// Check with both entity managers
		Assert.assertEquals("Test1", this.jtaTestBean.getNameTransJtaDb1(-1L));
		Assert.assertEquals("Test1", this.jtaTestBean.getNameTransJtaDb12(-1L));
	}
	
	@Test
	public void jta_canWriteData_withTransaction_usingTwoEntityManagers() {
		this.setNameJtaBoth("newName", false);
		
		// Check with both entity managers
		Assert.assertEquals("newName", this.jtaTestBean.getNameTransJtaDb1(-1L));
		Assert.assertEquals("newName", this.jtaTestBean.getNameTransJtaDb12(-1L));
	}
	
	@Test
	public void jta_canRollbackData_withTransaction_usingTwoEntityManagers() {
		this.setNameJtaBoth("newName", true);
		
		// Check with both entity managers
		Assert.assertEquals("Test1", this.jtaTestBean.getNameTransJtaDb1(-1L));
		Assert.assertEquals("Test1", this.jtaTestBean.getNameTransJtaDb12(-1L));
	}
	
	private void setNameWithRollbackJta1(String name) {
		try {
			this.jtaTestBean.setNameTransJtaDb1(-1L, name, true);
			Assert.fail("No Exception");
		} catch (RuntimeException ex) {
			// Expected
		}
	}
	
	private void setNameJtaBoth(String name, boolean rollback) {
		try {
			this.jtaTestBean.setNameTransJtaDb1And2(-1L, name, -1L, name, rollback);
			if (rollback) Assert.fail("No Exception");
		} catch (RuntimeException ex) {
			if (!rollback) throw ex;
			else; //expected
		}
	}
	
	@Test
	public void canUse_beanManagedTransaction() throws Exception {
		UserTransaction tx = this.ctx.getBean(UserTransaction.class);
		
		try {
			tx.begin();
			Assert.assertEquals("Test1", this.jtaTestBean.getNameNoTransJtaDb1(-1L));
			
			// Try rollback
			this.jtaTestBean.setNameTransJtaDb1(-1L, "newName", false);
			tx.rollback();
			
			// Check value
			Assert.assertEquals("Test1", this.jtaTestBean.getNameTransJtaDb1(-1L));
			
			// Try commit
			tx.begin();
			this.jtaTestBean.setNameTransJtaDb1(-1L, "newName", false);
			tx.commit();
			
			// Check value
			Assert.assertEquals("newName", this.jtaTestBean.getNameTransJtaDb1(-1L));
		} finally {
			if (tx.getStatus() != Status.STATUS_NO_TRANSACTION) tx.rollback();
		}
	}
	
	private void assumeTwoDbs() {
		DataSource ds = this.ctx.getBean("jpaDb2DataSource", DataSource.class);
		JuConnUtil connUtil = JuConnUtils.createByDataSource(ds);
		JuAssumeUtils.dbIsNot(connUtil, DbType.MYSQL, DbType.ORACLE);
	}
}
