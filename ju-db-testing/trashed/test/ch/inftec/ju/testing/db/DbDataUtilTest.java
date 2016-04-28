package ch.inftec.ju.testing.db;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuUrl;
import ch.inftec.ju.util.xml.XPathGetter;


/**
 * Test cases for the DbDataUtil methods.
 * @author Martin
 *
 */
public class DbDataUtilTest extends DefaultContextAbstractBaseDbTest {
	@Before
	public void loadTestData() {
		this.createDbDataUtil().cleanImport("/datasets/fullData.xml");
	}
	
	/**
	 * Tests the data export function writing DB data to an XML file.
	 */
	@Test
	public void writeToXmlFile() {
		File file = null;
		try {
			// Whole table to file
			String fileName = "writeToXmlFile_team.xml";
			file = new File(fileName);
			if (file.exists()) file.delete();
			this.createDbDataUtil().buildExport()
				.addTable("Team", null)
				.writeToXmlFile(fileName);
			
			Assert.assertTrue(file.exists());
		} finally {
			if (file != null && file.exists()) file.delete();
		}
	}
	
	/**
	 * Tests the data export function writing DB data to an XML Document.
	 */
	@Test
	public void writeToDocument() {
		// Whole table to XML Document
		Document doc = this.createDbDataUtil().buildExport()
			.addTable("Team", null)
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		Assert.assertEquals(2, xg.getSingleLong("count(//Team)").intValue());
	}
	
	@Test
	public void writeToDocument_query() {
		// Annotation not working as it would run after class annotation which results
		// in an INSERT instead of a CLEAN_INSERT
		this.createDbDataUtil().buildImport()
			.from("/datasets/testingEntityUnsortedData.xml")
			.executeCleanInsert();
		
		Document doc = this.createDbDataUtil().buildExport()
			.addTable("TestingEntity", "SELECT * FROM TESTINGENTITY WHERE ID=2")
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		Assert.assertEquals(1, xg.getSingleLong("count(//TestingEntity)").intValue());
		Assert.assertEquals(2, xg.getSingleLong("//TestingEntity/@id").intValue());
	}
	
	@Test
	public void writeToDocument_order() {
		// Annotation not working as it would run after class annotation which results
		// in an INSERT instead of a CLEAN_INSERT
		this.createDbDataUtil().buildImport()
			.from("/datasets/testingEntityUnsortedData.xml")
			.executeCleanInsert();
				
		Document doc = this.createDbDataUtil().buildExport()
			.addTableSorted("TestingEntity", "ID")
			.writeToXmlDocument();
		
		XPathGetter xg = new XPathGetter(doc);
		Assert.assertEquals(1, xg.getSingleLong("//TestingEntity[1]/@id").intValue());
		Assert.assertEquals(2, xg.getSingleLong("//TestingEntity[2]/@id").intValue());
		Assert.assertEquals(3, xg.getSingleLong("//TestingEntity[3]/@id").intValue());
	}
	
	/**
	 * Tests the data import from an XML file.
	 */
	@Test
	public void importDataFromXml() {
		Assert.assertEquals(1, em.createQuery("Select t from TestingEntity t").getResultList().size());
		
		this.createDbDataUtil().buildImport()
			.from(JuUrl.resource().relativeTo(DbDataUtilTest.class).get("DbDataUtilsTest_importDataFromXml.xml"))
			.executeCleanInsert();
		
		Assert.assertEquals(2, em.createQuery("Select t from TestingEntity t").getResultList().size());		
	}
	
	/**
	 * Asserts that the complete DB export equals the data in an XML file. 
	 */
	@Test
	public void assertEqualsAll() {
//		// Can be used to create full export XML
//		du.buildExport().writeToXmlFile("completeExport.xml");
		
		this.createDbDataUtil().buildAssert()
			.expected(JuUrl.resource().relativeTo(DbDataUtilTest.class).get("DbDataUtilsTest_assertEqualsAll.xml"))
			.assertEqualsAll();
	}
	
	/**
	 * Asserts that a specific table export equals the data in an XML file.
	 */
	@Test
	public void assertEqualsTables() {
//		du.buildExport()
//			.addTable("team", "select * from team order by name")
//			.writeToXmlFile("teamExport.xml");
		
		this.createDbDataUtil().buildAssert()
			.expected(JuUrl.resource().relativeTo(DbDataUtilTest.class).get("DbDataUtilsTest_assertEqualsTables.xml"))
			.assertEqualsTable("team", "name");
	}
}
