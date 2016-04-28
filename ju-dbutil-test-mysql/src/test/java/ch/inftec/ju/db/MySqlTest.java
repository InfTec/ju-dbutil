package ch.inftec.ju.db;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfo;
import ch.inftec.ju.db.JuConnUtil.MetaDataInfo.SchemaInfos;
import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Test class containing MySql specific tests.
 * <p>
 * In order for the EM transaction tests to work (AbstractDbTestTransactionTest), make sure that the MySQL engine for the table
 * TestingEntity supports transactions! (see https://inftec.atlassian.net/wiki/display/TEC/MySQL).
 * @author Martin
 *
 */
public class MySqlTest extends AbstractDbTest {
	@Test
	public void dbType_isReckognized_forEmUtil() {
		Assert.assertEquals(DbType.MYSQL, this.emUtil.getDbType());
	}
	
	@Test
	public void dbType_isReckognized_forConnUtil() {
		Assert.assertEquals(DbType.MYSQL, this.connUtil.getDbType());
	}
	
	/**
	 * Check if we can get the schema names for MySQL.
	 */
	@Test
	public void metaDataInfo_canGet_schemaNames() {
		SchemaInfos schemaInfos = this.connUtil.getMetaDataInfo().getSchemaInfos();
		Assert.assertNotNull(schemaInfos);

		String schemaName = this.connUtil.getMetaDataInfo().getSchemaInfo().getName();

		Assert.assertTrue(this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaNames().contains(schemaName));
		Assert.assertTrue(this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaNames().contains("information_schema"));
	}

	/**
	 * Make sure catalog is null for MySQL.
	 */
	@Test
	public void metaDataInfo_returnsNull_forCatalog() {
		List<SchemaInfo> schemaInfos = this.connUtil.getMetaDataInfo().getSchemaInfos().getSchemaInfos("information_schema", null);
		Assert.assertEquals(1, schemaInfos.size());
		Assert.assertNull(schemaInfos.get(0).getCatalog());
	}
}
