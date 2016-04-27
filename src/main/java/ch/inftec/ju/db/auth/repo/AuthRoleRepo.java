package ch.inftec.ju.db.auth.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import ch.inftec.ju.db.auth.entity.AuthRole;

/**
 * Spring repository interface for the Role entity.
 * @author Martin
 *
 */
public interface AuthRoleRepo extends CrudRepository<AuthRole, Long>{
	@Query("select r from AuthRole r where r.name=?1")
	AuthRole getByName(String name);
	
	@Query("select r from AuthRole r join r.users u where r.name=?1 and u.id=?2")
	AuthRole getByNameAndUsersId(String name, Long userId);
}
