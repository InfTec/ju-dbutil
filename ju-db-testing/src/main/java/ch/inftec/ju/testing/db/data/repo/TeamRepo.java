package ch.inftec.ju.testing.db.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ch.inftec.ju.testing.db.data.entity.Team;

/**
 * Spring data CrudRepository for the TestingEntity entity.
 * @author Martin
 *
 */
public interface TeamRepo extends JpaRepository<Team, Long> {
	/*
	 * EclipseLink has a problem when the query should be created dynamically. When
	 * we explicitly define the query, we don't have this problem.
	 * Hibernate seems to work in both cases.
	 */
	@Query("select t from Team t where t.name = ?1")
	Team getByName(String name);
}
