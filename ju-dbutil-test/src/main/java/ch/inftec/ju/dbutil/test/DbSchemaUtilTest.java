package ch.inftec.ju.dbutil.test;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbSchemaUtil;
import ch.inftec.ju.util.JuCollectionUtils;

public class DbSchemaUtilTest extends AbstractDbTest {
	@Test
	public void tablesAreCreated_usingLiquibaseExplicitly() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		Assert.assertFalse(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TestingEntity_LB"));
		
		su.runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/LiquibaseTestDataTest_testingEntity.xml");
		
		Assert.assertTrue(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TestingEntity_LB"));
	}
	
	@Test
	public void tablesAreCreated_usingFlywayExplicitly() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		Assert.assertFalse(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TESTINGENTITY_FW"));
		
		su.runFlywayMigration("db/DbSchemaUtilTest-migration");
		
		Assert.assertTrue(JuCollectionUtils.collectionContainsIgnoreCase(this.emUtil.getTableNames(), "TESTINGENTITY_FW"));
	}
	
	/**
	 * Check if we can use the replaceOrExists tag. Liquibase doesn't support it with Derby, throwing an Exception.
	 * Therefore we filter the change logs before submitting them to Liquibase.
	 */
	@Test
	public void liquibase_canUseReplaceOrExists() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		
		su.clearSchema();
		su.runLiquibaseChangeLog("ch/inftec/ju/dbutil/test/DbSchemaUtilTest_liquibase_canUseReplaceOrExists.xml");
	}

	/**
	 * Make sure we can set Liquibaes changeLog parameters.
	 */
	@Test
	public void canSet_changeLogParameters() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);

		su.clearSchema();
		su.liquibaseChangeLog()
				.changeLogResource("ch/inftec/ju/dbutil/test/DbSchemaUtilTest_liquibase_canUseChangeLogParameters.xml")
				.parameter("myTableName", "TestingEntity_LB")
				.parameter("myName", "FooBar")
				.run();

		Assert.assertEquals("FooBar", this.em.createNativeQuery("select max(name) from TestingEntity_LB").getSingleResult().toString());
	}
}
