package ch.inftec.ju.dbutil.test.jta;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.inftec.ju.db.JuEmUtil;
import ch.inftec.ju.testing.db.data.repo.TestingEntityRepo;

public class JtaTestBean {
	@PersistenceContext(unitName="jpaDb1Emf")
	private EntityManager emJpaDb1;
	
	@PersistenceContext(unitName="jpaDb1Emf2")
	private EntityManager emJpaDb12;
	
	@PersistenceContext(unitName="jpaDb2Emf")
	private EntityManager emJpaDb2;
	
	@PersistenceContext(unitName="jtaDb1Emf")
	private EntityManager emJtaDb1;
	
	@PersistenceContext(unitName="jtaDb1Emf2")
	private EntityManager emJtaDb12;
	
	@PersistenceContext(unitName="jtaDb2Emf")
	private EntityManager emJtaDb2;
	
	@Autowired
	private UserTransaction tx;

	public String getNameNoTransJpaDb1(Long id) {
		return this.getName(this.emJpaDb1, id);
	}
	
	public String getNameNoTransJpaDb12(Long id) {
		return this.getName(this.emJpaDb12, id);
	}
	
	public String getNameNoTransJpaDb2(Long id) {
		return this.getName(this.emJpaDb2, id);
	}
	
	public void setNameNoTransJpaDb1(Long id, String name) {
		this.setName(this.emJpaDb1, id, name);
	}
	
	@Transactional(value="tmJpaDb1Emf")
	public void setNameTransJpaDb1(Long id, String name, boolean rollback) {
		this.setName(this.emJpaDb1, id, name);
		if (rollback) throw new RuntimeException("Rollback");
	}
	
	@Transactional(value="tmJpaDb1Emf")
	public void setNameTransJpaDb1and2(Long id1, String name1, Long id2, String name2, boolean rollback) {
		this.setName(this.emJpaDb1, id1, name1);
		this.setName(this.emJpaDb2, id2, name2);
		if (rollback) throw new RuntimeException("Rollback");
	}
	
	public String getNameNoTransJtaDb1(Long id) {
		return this.getName(this.emJtaDb1, id);
	}
	
	@Transactional
	public String getNameTransJtaDb1(Long id) {
		return this.getName(this.emJtaDb1, id);
	}
	
	@Transactional
	public String getNameTransJtaDb12(Long id) {
		return this.getName(this.emJtaDb12, id);
	}
	
	@Transactional
	public void setNameTransJtaDb1(Long id, String name, boolean rollback) {
		this.setName(this.emJtaDb1, id, name);
		if (rollback) throw new RuntimeException("Rollback");
	}
	
	@Transactional
	public void setNameTransJtaDb1And2(Long id1, String name1, Long id2, String name2, boolean rollback) {
		this.setName(this.emJtaDb1, id1, name2);
		this.setName(this.emJtaDb2, id2, name2);
		if (rollback) throw new RuntimeException("Rollback");
	}
	
	private String getName(EntityManager em, Long testingEntityId) {
		TestingEntityRepo teRepo = new JuEmUtil(em).getJpaRepository(TestingEntityRepo.class);
		return teRepo.findOne(testingEntityId).getName();
	}
	
	private void setName(EntityManager em, Long testingEntityId, String name) {
		TestingEntityRepo teRepo = new JuEmUtil(em).getJpaRepository(TestingEntityRepo.class);
		teRepo.findOne(testingEntityId).setName(name);
	}
}
