package ch.inftec.ju.testing.db.data;

import java.util.List;

import javax.persistence.Query;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.testing.db.DefaultContextAbstractBaseDbTest;
import ch.inftec.ju.testing.db.data.entity.Player;
import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Tests to test a TestDb instance.
 * @author tgdmemae
 *
 */
public class TestDbTest extends DefaultContextAbstractBaseDbTest {
	@Before
	public void loadTestData() {
		this.createDbDataUtil().cleanImport("/datasets/fullData.xml");
	}
	
	@Autowired
	private JuDbUtils juDbUtils;

	/**
	 * Tests if the table TEST_A has been created correctly.
	 */
	@Test
	public final void testA() {		
		List<String> columnNames = this.juDbUtils.getColumnNames("TEST_A");
			
		Assert.assertEquals(3, columnNames.size());
		Assert.assertTrue(columnNames.containsAll(JuCollectionUtils.arrayList("AID", "TEXT", "B_FK")));
		
		int cnt = this.jdbcTemplate.queryForInt("select count(*) from test_a");
		Assert.assertEquals(3, cnt);
	}
	
	/**
	 * Tests if the JPA entities (Team, Player) have been created correctly.
	 */
	@Test
	public final void testEntities() {		
		Query q = em.createQuery("select p from Player p where p.firstName='All' and p.lastName='Star'");
		Player allstar = (Player)q.getSingleResult();		
		
		Assert.assertEquals("AllStar", allstar.getFirstName() + allstar.getLastName());
		Assert.assertEquals(2, allstar.getTeams().size());
		Assert.assertEquals(3, allstar.getTeams().iterator().next().getPlayers().size());
	}
}
