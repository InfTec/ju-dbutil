package ch.inftec.ju.db;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import junit.framework.Assert;

import org.junit.Test;

import ch.inftec.ju.testing.db.data.entity.TestingEntity;

public class EntityManagerFactoryTest {
	@Test
	public void canCreateEntityManager_fromSinglePersistenceXml_fromDependency() {
		EntityManager em = Persistence.createEntityManagerFactory("ju pu-testingEntity").createEntityManager();
		
		TestingEntity te = new TestingEntity();
		em.getTransaction().begin();
		em.persist(te);
		em.flush();
		em.getTransaction().commit();
		em.close();
		
		Assert.assertNotNull(te.getId());
	}

	/**
	 * We have two META-INF/persistence.xml files in this project setup, one in ju-testing (with ju pu-testingEntity)
	 * and one in ju-dbutil-test (with ju pu-testingEntity2).
	 * This test shows that all persistence.xml files are taken into account when looking for a persistence unit.
	 */
	@Test
	public void canCreateEntityManager_fromSinglePersistenceXml_fromCurrentProject() {
		EntityManager em = Persistence.createEntityManagerFactory("ju pu-testingEntity2").createEntityManager();
		
		TestingEntity te = new TestingEntity();
		em.getTransaction().begin();
		em.persist(te);
		em.flush();
		em.getTransaction().commit();
		em.close();
		
		Assert.assertNotNull(te.getId());
	}
}