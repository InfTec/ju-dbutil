package ch.inftec.ju.testing.db;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.testing.db.data.entity.TestingEntity;

public class EntityManagerFactoryTest {
	@Test
	public void canCreateEntityManager_fromSinglePersistenceXml() {
		EntityManager em = Persistence.createEntityManagerFactory("ju pu-testingEntity").createEntityManager();
		
		TestingEntity te = new TestingEntity();
		em.getTransaction().begin();
		em.persist(te);
		em.flush();
		em.getTransaction().commit();
		
		Assert.assertNotNull(te.getId());
	}
}
