package ch.inftec.ju.db.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.db.data.entity.CustomObject;

/**
 * Class to test basic DB connection related functionality.
 * @author Martin
 *
 */
@ContextConfiguration(locations="classpath:/ch/inftec/ju/db/ju-util-context.xml")
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
public class DbConnectionTest {
	@Autowired
	private ConnectionInfo connectionInfo;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private DataSource dataSource;
	
	/**
	 * Use the EntityManager to make sure the DB is initiated.
	 */
	@Before
	public void initDb() {
		this.entityManager.getMetamodel();
	}
	
	@Test
	public void dbConnection() {
		Assert.assertEquals("Derby Test DB", this.connectionInfo.getName());
		Assert.assertNull(this.connectionInfo.getSchema());
		
		CustomObject o = new CustomObject();
		this.entityManager.persist(o);
		Assert.assertNotNull(o.getId());
	}
	
	@Test
	public void jdbcTemplate() {
		Assert.assertEquals(1, this.jdbcTemplate.update("insert into CustomObject (id, text) values (-1, 'test')"));
		
		Map<String, Object> o = this.jdbcTemplate.queryForMap("select id, text from CustomObject where id=-1");
		Assert.assertEquals(-1L, o.get("id"));
		Assert.assertEquals("test", o.get("text"));
	}
	
	@Test
	public void dataSource() throws Exception {
		Assert.assertEquals(1, this.jdbcTemplate.update("insert into CustomObject (id, text) values (-1, 'test')"));
		
		// Check if the Connection returned by DataSource lets us get the inserted object
		Connection conn = DataSourceUtils.getConnection(this.dataSource);
		PreparedStatement stmt = conn.prepareStatement("select count(*) from CustomObject where id=-1");
		ResultSet rs = stmt.executeQuery();
		Assert.assertTrue(rs.next());
		Assert.assertEquals(1, rs.getInt(1));
		Assert.assertFalse(rs.next());
		
		stmt.close();
		rs.close();
		// conn.close(); Must not close Connection
	}
	
//	/**
//	 * Tests the creation of a DbConnectionFactory using the default connection.xml path.
//	 */
//	@Test
//	public void connectionFactoryFromXml() throws Exception {
//		DbConnectionFactory factory = DbConnectionFactoryLoader.createInstance();
//		
//		// Test available connections
//		Assert.assertEquals(3, factory.getAvailableConnections().size());
//		TestUtils.assertCollectionEquals(
//				JuCollectionUtils.arrayList("ESW MyTTS", "Derby InMemory-DB", "PU_mementoObject"), 
//				factory.getAvailableConnections());
//				
//		// Test flags
//		Assert.assertEquals(2, factory.getAvailableConnections("connection").size());
//		List<String> eswConnections = factory.getAvailableConnections("esw");
//		Assert.assertEquals(1, eswConnections.size());
//		Assert.assertEquals("ESW MyTTS", eswConnections.get(0));
//		eswConnections = factory.getAvailableConnections("esw", "connection");
//		Assert.assertEquals(1, eswConnections.size());
//		Assert.assertEquals("ESW MyTTS", eswConnections.get(0));
//		
//		// Try getting a connection
//		
//		try (DbConnection derbyConnection = factory.openDbConnection("Derby InMemory-DB")) {
//			Assert.assertEquals("Derby InMemory-DB", derbyConnection.getName());
//		}
//	}
//	
//	/**
//	 * Tests the creation of a DbConnectionFactory using a custom persistence.xml path.
//	 */
//	@Test
//	public void connectionFactoryFromCustomXml() throws Exception {
//		DbConnectionFactory factory = DbConnectionFactoryLoader.createInstance("/META-INF/customPersistence.xml");
//		
//		// Try getting a connection
//		
//		try (DbConnection derbyConnection = factory.openDbConnection("CustomDerby")) {
//			Assert.assertEquals("CustomDerby", derbyConnection.getName());
//			
//			// Store CustomObject
//			EntityManager em = derbyConnection.getEntityManager();
//			CustomObject co = new CustomObject();
//			co.setText("Test");
//			em.persist(co);
//			em.flush();
//		}
//	}
}
