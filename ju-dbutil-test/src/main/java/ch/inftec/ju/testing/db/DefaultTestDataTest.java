package ch.inftec.ju.testing.db;

import java.text.ParseException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.testing.db.data.entity.DataTypes;
import ch.inftec.ju.util.JuStringUtils;

public class DefaultTestDataTest extends AbstractDbTest {
	@Test
	public void tableStructure_canBeCreatedUsingLiquibase() {
		DbSchemaUtil su = new DbSchemaUtil(this.em);
		su.clearSchema();
		
		su.runLiquibaseChangeLog("ju-testing/data/default-changeLog.xml");
	}
	
	@Test
	public void defaultTestData_canBeLoaded() {
		new DbSchemaUtil(this.em).prepareDefaultSchemaAndTestData();
	}
	
	@Test
	public void defaultTestData_canBeLoadedTwice() {
		new DbSchemaUtil(this.em).prepareDefaultSchemaAndTestData();
		new DbSchemaUtil(this.em).prepareDefaultSchemaAndTestData();
	}
	
	@Test
	public void canRead_allDataTypes_fromDefaultTestData() throws ParseException {
		new DbSchemaUtil(this.em).prepareDefaultSchemaAndTestData();
		
		DataTypes dt1 = this.em.find(DataTypes.class, -1L);
		
		Assert.assertEquals(new Integer(1), dt1.getIntNumber());
		Assert.assertEquals(new Long(2), dt1.getBigIntNumber());
		
		Assert.assertEquals("one", dt1.getVarcharText());
		Assert.assertEquals("oneClob", dt1.getClobText());
		
		Assert.assertEquals(JuStringUtils.DATE_FORMAT_DAYS.parseObject("03.12.1980"), dt1.getDateField());
		
		// We'll skip TIME checking for Oracle as it isn't supported by this DB
		if (this.emUtil.getDbType() != DbType.ORACLE) {
			String hours = JuStringUtils.DATE_FORMAT_SECONDS.format(dt1.getTimeField());
			Assert.assertTrue(hours.endsWith("10:11:12")); // hours will be todays date, followed by the time
		}
		
		Assert.assertEquals(JuStringUtils.DATE_FORMAT_SECONDS.parseObject("03.12.1980 10:11:12"), dt1.getTimeStampField());
	}
	
	@Test
	public void canRead_allNullValues_fromDefaultTestData() throws ParseException {
		new DbSchemaUtil(this.em).prepareDefaultSchemaAndTestData();
		
		DataTypes dt2 = this.em.find(DataTypes.class, -2L);
		
		Assert.assertNull(dt2.getIntNumber());
		Assert.assertNull(dt2.getBigIntNumber());
		
		Assert.assertNull(dt2.getVarcharText());
		Assert.assertNull(dt2.getClobText());
		
		Assert.assertNull(dt2.getDateField());
		Assert.assertNull(dt2.getTimeField());

		// MySQL sets a TimeStamp field to the current time unless it has been created
		// explicitly with a NULL attribute which Liquibase doesn't do.
		Assume.assumeTrue(this.emUtil.getDbType() != DbType.MYSQL);
		Assert.assertNull(dt2.getTimeStampField());
	}
}
