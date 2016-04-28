package ch.inftec.ju.testing.db;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.testing.db.data.entity.TestingEntity;

public class LiquibaseTest extends AbstractDbTest {
	@JuDbTest(profile="derby-lb-create", persistenceUnit="ju-testing-pu-liquibase")
	@Test
	public void canGenerateSchema_usingLiquibase() {
		Assert.assertEquals(0, this.emUtil.getTableNames().size());
		
		new DbSchemaUtil(this.em).runLiquibaseChangeLog("ch/inftec/ju/testing/db/LiquibaseTest_testingEntityChangeLog.xml");
		
		assertThat(this.emUtil.getTableNames(), hasItem("TESTINGENTITY"));
	}
	
	@JuDbTest(profile="derby-lb-change", persistenceUnit="ju-testing-pu-liquibase")
	@Test
	public void canExecute_dbUnitDataSet_asLiquibaseChangeSet() {
		Assert.assertEquals(0, this.emUtil.getTableNames().size());
		
		new DbSchemaUtil(this.em).runLiquibaseChangeLog("ch/inftec/ju/testing/db/LiquibaseTest_dbUnitChangeLog.xml");
		
		TestingEntity te = this.em.find(TestingEntity.class, 1L);
		Assert.assertEquals("LiquibaseDbUnitChangeSet", te.getName());
	}

	/**
	 * This tests data loading from an external file, as well as locating that file in a relative
	 * sub folder.
	 */
	@JuDbTest(profile="derby-lb-import", persistenceUnit="ju-testing-pu-liquibase")
	@Test
	public void canImportData_fromExternalFile_inRelativeSubFolder() {
		Assert.assertEquals(0, this.emUtil.getTableNames().size());
		
		new DbSchemaUtil(this.em).runLiquibaseChangeLog("ch/inftec/ju/testing/db/LiquibaseTest_loadFromExternalFile.xml");
		
		TestingEntity te = this.em.find(TestingEntity.class, 1L);
		Assert.assertEquals("Loaded from File", te.getName());
	}
}
