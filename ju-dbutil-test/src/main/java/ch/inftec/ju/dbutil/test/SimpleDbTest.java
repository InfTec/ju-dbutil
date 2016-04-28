package ch.inftec.ju.dbutil.test;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractDbTest;

/**
 * Simple test case to verify DB access.
 * @author Martin
 *
 */
public class SimpleDbTest extends AbstractDbTest {
	@Test
	public void assert_entityManager_isOpen() {
		Assert.assertTrue(this.em.isOpen());
	}
}
