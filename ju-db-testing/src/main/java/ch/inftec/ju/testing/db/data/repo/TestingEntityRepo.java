package ch.inftec.ju.testing.db.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ch.inftec.ju.testing.db.data.entity.TestingEntity;

/**
 * Spring data CrudRepository for the TestingEntity entity.
 * @author Martin
 *
 */
public interface TestingEntityRepo extends JpaRepository<TestingEntity, Long> {
	/*
	 * EclipseLink has a problem when the query should be created dynamically. When
	 * we explicitly define the query, we don't have this problem.
	 * Hibernate seems to work in both cases.
	 */
	@Query("select t from TestingEntity t where t.name = ?1")
	TestingEntity getByName(String name);
}
