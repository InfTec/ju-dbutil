package ch.inftec.ju.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.persistence.Query;

import org.hibernate.jdbc.Work;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfos;
import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Test class containing Derby specific tests.
 * @author Martin
 *
 */
public class OracleTest extends AbstractDbTest {
	@Test
	public void dbType_isReckognized_forEmUtil() {
		Assert.assertEquals(DbType.ORACLE, this.emUtil.getDbType());
	}
	
	@Test
	public void dbType_isReckognized_forConnUtil() {
		Assert.assertEquals(DbType.ORACLE, this.connUtil.getDbType());
	}
	
	@Test
	public void canEvaluateDefaultSchema() {
		Query qry = this.em.createNativeQuery("select sys_context( 'userenv', 'current_schema' ) from dual");
		Object res = qry.getSingleResult();
		Assert.assertNotNull(res);
	}
	
	/**
	 * Fails on Oracle XE as well as on Oracle enterprise edition
	 */
	@Ignore
	@Test
	public void canEvaluateDefaultSchema_usingPrepareCall() {
		this.emUtil.doWork(new Work() {
			@Override
			public void execute(Connection connection) throws SQLException {
				CallableStatement cs = connection.prepareCall("select sys_context( 'userenv', 'current_schema' ) from dual");
				ResultSet rs = cs.executeQuery();
				rs.next();
			}
		});
	}

	/**
	 * Check if we can get the schema names for Oracle.
	 */
	@Test
	public void metaDataInfo_canGet_schemaNames() {
		SchemaInfos schemaInfos = this.connUtil.getMetaDataInfo().getSchemaInfos();
		Assert.assertNotNull(schemaInfos);

		String schemaName = this.connUtil.getMetaDataInfo().getUserName();

		Assert.assertTrue(this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaNames().contains(schemaName));
		Assert.assertTrue(this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaNames().contains("SYS"));
		Assert.assertTrue(this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaNames().contains("SYSTEM"));
	}

	/**
	 * Make sure catalog is null for Oracle.
	 */
	@Test
	public void metaDataInfo_returnsNull_forCatalog() {
		List<SchemaInfo> schemaInfos = this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaInfos("SYS", null);
		Assert.assertEquals(1, schemaInfos.size());
		Assert.assertNull(schemaInfos.get(0).getCatalog());
	}
}
