package ch.inftec.ju.dbutil.test;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.EmfWork;
import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DataSet;
import ch.inftec.ju.testing.db.DataSetExport;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;
import ch.inftec.ju.util.JuCollectionUtils;

public class AbstractDbTestAnnotationTest extends AbstractDbTest {
	@Override
	protected void runDbInitializationScripts(JuEmfUtil emfUtil) {
		try (EmfWork ew = emfUtil.startWork()) {
			new DbSchemaUtil(ew.getEm()).prepareDefaultSchemaAndTestData();;
		}
	}
	
	@Test
	public void entityManager_isLoaded() {
		Assert.assertTrue(this.em.isOpen());
	}
	
	@Test
	public void liquibaseScript_isExecuted() {
		Assert.assertTrue(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TestingEntity"));
	}
	
	@Test
	@DataSet("AbstractDbTestAnnotationTest_testingEntity.xml")
	public void dataSet_isLoaded() {
		Assert.assertEquals("AbstractDbTestAnnotationTest1", this.em.find(TestingEntity.class, -1L).getName());
	}
	
	@Test
	@DataSet("AbstractDbTestAnnotationTest_testingEntity.xml")
	@DataSetExport(tablesDataSet="AbstractDbTestAnnotationTest_testingEntity.xml")
	public void dataSet_isExported() {
		this.em.find(TestingEntity.class, -1L).setName("dataSet_isExported");
	}
}