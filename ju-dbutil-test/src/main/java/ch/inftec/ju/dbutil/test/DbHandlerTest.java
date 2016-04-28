package ch.inftec.ju.dbutil.test;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.testing.db.AbstractDbTest;
import ch.inftec.ju.testing.db.DbSchemaUtil;

public class DbHandlerTest extends AbstractDbTest {
	@Test
	public void canResetIdentityGeneration_forPrimeryKeys() {
		new DbSchemaUtil(this.emUtil).prepareDefaultSchemaAndTestData();
		
		String lowerStmt = this.connUtil.getDbHandler().wrapInLowerString("te.name");
		
		Query q = this.em.createNativeQuery(String.format(
				"select %s as lowName, te.name from TestingEntity te where %s = 'test1'" // Also test if the statement works in comparisons
				, lowerStmt, lowerStmt));
		
		Assert.assertEquals("test1", ((Object[]) q.getResultList().get(0))[0].toString());
		Assert.assertEquals("Test1", ((Object[]) q.getResultList().get(0))[1].toString());
	}
}
