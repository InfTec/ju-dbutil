package ch.inftec.ju.db;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.db.JuConnUtil.DbType;
import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Test class containing Derby specific tests.
 * @author Martin
 *
 */
public class DerbyTest extends AbstractDbTest {
	@Test
	public void dbType_isReckognized_forEmUtil() {
		Assert.assertEquals(DbType.DERBY, this.emUtil.getDbType());
	}
	
	@Test
	public void dbType_isReckognized_forConnUtil() {
		Assert.assertEquals(DbType.DERBY, this.connUtil.getDbType());
	}
}
