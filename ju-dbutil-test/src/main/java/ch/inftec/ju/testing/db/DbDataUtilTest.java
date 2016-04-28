package ch.inftec.ju.testing.db;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.ComparisonFailure;

import org.hibernate.jdbc.Work;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;
import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.JuUrl;
import ch.inftec.ju.util.TestUtils;
import ch.inftec.ju.util.xml.XPathGetter;
import ch.inftec.ju.util.xml.XmlOutputConverter;
import ch.inftec.ju.util.xml.XmlUtils;

public class DbDataUtilTest extends AbstractDbTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void canImportData_fromDatasetFile() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		DbDataUtil du = new DbDataUtil(this.em);
		du.cleanImport("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity.xml");
		TestingEntity te = this.em.find(TestingEntity.class, 1L);
		Assert.assertEquals("DbDataUtilTest", te.getName());
	}
	
	@Test
	public void canExportData_toXmlDocument() {
		this.prepareExportData();
		
		DbDataUtil du = new DbDataUtil(this.em);
		Document doc = du.buildExport().addTable("TestingEntity").writeToXmlDocument();
		this.assertXmlDocumentExport(doc);
	}
	
	private void prepareExportData() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		this.em.flush(); // Not all DBs require a flush here, but it's safer
	}
	
	private void assertXmlDocumentExport(Document doc) {
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TestingEntity/@name"));
	}
	
	@Test
	public void canExportData_toXmlOutputConverter() throws Exception {
		this.prepareExportData();
		
		DbDataUtil du = new DbDataUtil(this.em);
		XmlOutputConverter xmlOutput = du.buildExport().addTable("TestingEntity").writeToXml();
		
		// Make sure we can get the document
		this.assertXmlDocumentExport(xmlOutput.getDocument());
		
		// Make sure we can get the XML String
		TestUtils.assertEqualsResource("DbDataUtilTest_canExportData_toXmlOutputConverter.xml", xmlOutput.getXmlString());
	}
	
	@Test
	public void xmlExport_copesWithCamelCaseTable_andUsesUpperCaseColumnNames() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		this.em.flush(); // Not all DBs require a flush here, but it's safer
		
		// Export table with camel case
		DbDataUtil du = new DbDataUtil(this.em);
		Document doc = du.buildExport().addTable("TestingEntity").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TestingEntity/@name"));
	}
	
	@Test
	public void xmlExport_copesWithLowerCaseTable_andUsesUpperCaseColumnNames() {
		// MySQL is case sensitive
		JuAssumeUtils.dbIsNot(this.emUtil, DbType.MYSQL);
				
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		this.em.flush(); // Not all DBs require a flush here, but it's safer
		
		// Export table with camel case
		DbDataUtil du = new DbDataUtil(this.em);
		Document doc = du.buildExport().addTable("testingentity").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//testingentity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//testingentity/@name"));
	}
	
	@Test
	public void xmlExport_copesWithUpperCaseTable_andUsesUpperCaseColumnNames() {
		// MySQL is case sensitive
		JuAssumeUtils.dbIsNot(this.emUtil, DbType.MYSQL);
		
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		this.em.flush(); // Not all DBs require a flush here, but it's safer
		
		// Export table with camel case
		DbDataUtil du = new DbDataUtil(this.em);
		Document doc = du.buildExport().addTable("TESTINGENTITY").writeToXmlDocument();
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TESTINGENTITY").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TESTINGENTITY/@name"));
	}
	
	@Test
	public void xmlExport_canApply_casedTableNames() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		this.em.flush(); // Not all DBs require a flush here, but it's safer
		
		// Export table with camel case
		DbDataUtil du = new DbDataUtil(this.em);
		Document doc = du.buildExport()
			.addTable("TESTINGENTITY")
			.setTableNamesCasingByDataSet("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity.xml")
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TestingEntity/@name"));
	}
	
	@Test
	public void canExportTables_basedOnDatasetXml() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		TestingEntity te = new TestingEntity();
		te.setName("Export Test");
		this.em.persist(te);
		this.em.flush(); // Not all DBs require a flush here, but it's safer
		
		// Export table with camel case
		DbDataUtil du = new DbDataUtil(this.em);
		Document doc = du.buildExport()
			.addTablesByDataSet(JuUrl.resource("ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity.xml"), false)
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(1, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("Export Test", xg.getSingle("//TestingEntity/@name"));
	}
	
	@Test
	public void exportTables_areSortedByPrimaryKey() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		DbDataUtil du = new DbDataUtil(this.em);
		du.cleanImport("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity_unsorted.xml");
		
		// Export table with camel case
		Document doc = du.buildExport()
			.addTableSorted("TestingEntity")
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(3, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("1", xg.getSingle("//TestingEntity[1]/@id"));
		Assert.assertEquals("2", xg.getSingle("//TestingEntity[2]/@id"));
		Assert.assertEquals("3", xg.getSingle("//TestingEntity[3]/@id"));
	}
	
	@Test
	public void exportTables_basedOnDatasetXml_areSortedByPrimaryKey() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		DbDataUtil du = new DbDataUtil(this.em);
		du.cleanImport("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity_unsorted.xml");
		
		// Export table with camel case
		Document doc = du.buildExport()
			.addTablesByDataSet(JuUrl.resource("ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity.xml"), true)
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		logger.debug("Exported XML\n" + XmlUtils.toString(doc, false, true));
		
		Assert.assertEquals(3, xg.getArray("//TestingEntity").length);
		Assert.assertEquals("1", xg.getSingle("//TestingEntity[1]/@id"));
		Assert.assertEquals("2", xg.getSingle("//TestingEntity[2]/@id"));
		Assert.assertEquals("3", xg.getSingle("//TestingEntity[3]/@id"));
	}
	
	@Test
	public void assert_canAssertTables() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		DbDataUtil du = new DbDataUtil(this.emUtil);
		du.cleanImport("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity_unsorted.xml");
		
		du.buildAssert()
			.expected("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity_sorted.xml")
			.assertEquals();
	}
	
	@Test(expected=ComparisonFailure.class)
	public void assert_canAssertTables_failsOnWrongData() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		DbDataUtil du = new DbDataUtil(this.emUtil);
		du.cleanImport("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity_unsorted.xml");
		
		du.buildAssert()
			.expected("/ch/inftec/ju/testing/db/DbDataUtilTest_testingEntity.xml")
			.assertEquals();
	}
	
	@Test
	public void canImport_usingJdbcConnection() {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.prepareDefaultTestData(true, true, true);
		
		this.emUtil.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				URL url = JuUrl.resource("ch/inftec/ju/testing/db/DbDataUtilTest_jdbcConnectionImport.xml");
				DbDataUtil.executeInsert(connection, url, true);
			}
		});
		
		TestingEntity te = this.em.find(TestingEntity.class, 1L);
		Assert.assertEquals("DbDataUtilTest.jdbcConnection", te.getName());
	}
	
	@Test
	public void supports_automatedSorting_ofDataSets_forDelete() {
		this.testAutomatedSorting_forDelete(true);
	}
	
	@Test
	public void fails_withoutAutomatedSorting_forDelete() {
		// Obviously not a problem on MySQL
		JuAssumeUtils.dbIsNot(this.connUtil, DbType.MYSQL);
		
		this.thrown.expect(JuRuntimeException.class);
		this.testAutomatedSorting_forDelete(false);
	}
	
	private void testAutomatedSorting_forDelete(boolean automatedOrder) {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.runLiquibaseChangeLog("ch/inftec/ju/testing/db/DbDataUtilTest_foreignKey_changeLog.xml");
		
		DbDataUtil du = new DbDataUtil(this.em);
		
		du.buildImport()
			.from("DbDataUtilTest_supports_automatedSorting_ofDataSets_forDelete_initialData.xml")
			.executeCleanInsert();
		
		du.buildImport()
			.automatedTableOrder(automatedOrder)
			.from("DbDataUtilTest_supports_automatedSorting_ofDataSets_forDelete.xml")
			.executeDeleteAll();
	}
	
	@Test
	public void supports_automatedSorting_ofDataSets_forImport() {
		this.testAutomatedSorting_forInsert(true);
	}
	
	@Test
	public void fails_withoutAutomatedSorting_forInsert() {
		// Obviously not a problem on MySQL
		JuAssumeUtils.dbIsNot(this.connUtil, DbType.MYSQL);
				
		this.thrown.expect(JuRuntimeException.class);
		this.testAutomatedSorting_forInsert(false);
	}
	
	private void testAutomatedSorting_forInsert(boolean automatedOrder) {
		DbSchemaUtil ds = new DbSchemaUtil(this.em);
		ds.runLiquibaseChangeLog("ch/inftec/ju/testing/db/DbDataUtilTest_foreignKey_changeLog.xml");
		
		DbDataUtil du = new DbDataUtil(this.em);
		
		du.buildImport()
			.automatedTableOrder(automatedOrder)
			.from("DbDataUtilTest_supports_automatedSorting_ofDataSets_forInsert.xml")
			.executeCleanInsert();
	}
}
