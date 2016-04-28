package ch.inftec.ju.db;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Test class containing HSQL specific tests.
 * 
 * @author Martin
 *
 */
public class HsqlTest extends AbstractDbTest {
	@Test
	public void dbType_isReckognized_forEmUtil() {
		Assert.assertEquals(DbType.HSQL, this.emUtil.getDbType());
	}
	
	@Test
	public void dbType_isReckognized_forConnUtil() {
		Assert.assertEquals(DbType.HSQL, this.connUtil.getDbType());
	}
}
