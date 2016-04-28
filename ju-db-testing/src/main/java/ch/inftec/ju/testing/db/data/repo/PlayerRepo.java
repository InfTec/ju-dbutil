package ch.inftec.ju.testing.db.data.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ch.inftec.ju.testing.db.data.entity.Player;

/**
 * Spring data CrudRepository for the Player entity.
 * @author Martin
 *
 */
public interface PlayerRepo extends JpaRepository<Player, Long> {
	/*
	 * EclipseLink has a problem when the query should be created dynamically. When
	 * we explicitly define the query, we don't have this problem.
	 * Hibernate seems to work in both cases.
	 */
	@Query("select p from Player p where p.lastName = ?1")
	Player getByLastName(String name);
}
