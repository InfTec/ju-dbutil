package ch.inftec.ju.db;

import javax.persistence.Query;

import org.junit.Assert;
import org.junit.Test;

import ch.inftec.ju.util.AssertUtil;

public class JuEmfUtilTest {
	@Test
	public void builder_canBuildEmfUtil_forDerby() {
		JuEmfUtil emfUtil = JuEmfUtil.create()
			.persistenceUnitName("ju-dbutil JuEmfUtilTest")
			.connectionUrl("jdbc:derby:memory:ju-dbutil_emfUtilTest_db1;create=true")
			.build();
		
		AssertUtil.assertNotNull(emfUtil);
	}
	
	@Test
	public void builder_returnsSameInstance_forSameProps() {
		JuEmfUtil emfUtil1 = JuEmfUtil.create()
			.persistenceUnitName("ju-dbutil JuEmfUtilTest")
			.connectionUrl("jdbc:derby:memory:ju-dbutil_emfUtilTest_db1;create=true")
			.build();
		
		JuEmfUtil emfUtil1b = JuEmfUtil.create()
			.persistenceUnitName("ju-dbutil JuEmfUtilTest")
			.connectionUrl("jdbc:derby:memory:ju-dbutil_emfUtilTest_db1;create=true")
			.build();
		
		Assert.assertSame(emfUtil1, emfUtil1b);
		
		JuEmfUtil emfUtil2 = JuEmfUtil.create()
			.persistenceUnitName("ju-dbutil JuEmfUtilTest")
			.connectionUrl("jdbc:derby:memory:ju-dbutil_emfUtilTest_db2;create=true")
			.build();
		
		Assert.assertNotSame(emfUtil1, emfUtil2);
	}
	
	@Test
	public void canPerformWork_usingEmfUtil() {
		JuEmfUtil emfUtil = JuEmfUtil.create()
			.persistenceUnitName("ju-dbutil JuEmfUtilTest")
			.connectionUrl("jdbc:derby:memory:ju-dbutil_emfUtilTest_db1;create=true")
			.build();
		
		try (EmfWork work = emfUtil.startWork()) {
			Query q = work.getEm().createNativeQuery("values 1");
			Assert.assertEquals(1, q.getSingleResult());
		}
	}
}
