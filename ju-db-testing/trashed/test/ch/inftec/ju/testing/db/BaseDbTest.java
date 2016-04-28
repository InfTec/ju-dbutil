package ch.inftec.ju.testing.db;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.testing.db.data.entity.Team;
import ch.inftec.ju.testing.db.data.entity.TestingEntity;
import ch.inftec.ju.util.IOUtil;
import ch.inftec.ju.util.JuUrl;

/**
 * Test cases for the AbstractBaseDb base class.
 * @author Martin
 *
 */
public class BaseDbTest extends DefaultContextAbstractBaseDbTest {
	/**
	 * Test case to assert that primary key sequences will be reset to
	 * produce predictable IDs.
	 */
	@Test
	public void sequenceReset1() {
		this.sequenceReset();
	}
	
	/**
	 * Same as sequenceReset1, to make sure order isn't important.
	 */
	@Test
	public void sequenceReset2() {
		this.sequenceReset();
	}
	
	/**
	 * Creates two entities and checks if they got the default start sequence of 10.
	 */
	private void sequenceReset() {
		TestingEntity te = new TestingEntity();
		em.persist(te);
		
		Assert.assertEquals(10L, te.getId().longValue());
		
		Team t = new Team();
		em.persist(t);
		
		Assert.assertEquals(10L, t.getId().longValue());
	}
	
	/**
	 * Makes sure that a DefaultDataSet of NONE contains no data.
	 */
	@Test
	public void noData() {
		this.createDbDataUtil().buildAssert()
			.expected(JuUrl.resource().relativeTo(BaseDbTest.class).get("BaseDbTest_noData.xml"))
			.assertEqualsAll();
	}
	
	@Test
	public void singleTestingEntityData() {
		Assert.assertEquals(0, em.createQuery("select t from TestingEntity t").getResultList().size());
		
		this.createDbDataUtil().buildImport()
			.from("/datasets/singleTestingEntityData.xml")
			.executeInsert();
		
		Assert.assertEquals(1, em.createQuery("select t from TestingEntity t").getResultList().size());
		
		this.createDbDataUtil().buildAssert()
			.expected(JuUrl.resource().relativeTo(BaseDbTest.class).get("BaseDbTest_singleTestingEntityData.xml"))
			.assertEqualsTable("TestingEntity", "id");
	}
}
