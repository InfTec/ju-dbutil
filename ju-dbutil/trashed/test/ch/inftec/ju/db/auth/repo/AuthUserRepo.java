package ch.inftec.ju.db.auth.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.inftec.ju.db.auth.entity.AuthUser;

/**
 * Spring repository interface for the User entity.
 * @author Martin
 *
 */
public interface AuthUserRepo extends CrudRepository<AuthUser, Long> {
	@Query("select u from AuthUser u where u.name=?1")
	AuthUser getByName(String name);
	
	/**
	 * Gets all AuthUsers, ordered by the user name.
	 * @return
	 */
	@Query("select u from AuthUser u order by u.name")
	List<AuthUser> findAll();
	
	/**
	 * Gets all user names, ordered alphabetically
	 * @return
	 */
	@Query("select u.name from AuthUser u order by u.name")
	List<String> findAllNames();
}
