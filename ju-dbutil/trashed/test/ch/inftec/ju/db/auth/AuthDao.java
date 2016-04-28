package ch.inftec.ju.db.auth;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import ch.inftec.ju.db.JuDbUtils;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthRoleRepo;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;

/**
 * Helper class for the AuthUser and AuthRole entities.
 * @author Martin
 *
 */
public class AuthDao {
	@PersistenceContext(unitName="juAuth")
	private EntityManager em;
	
	/**
	 * Adds the specified role to the User.
	 * <p>
	 * If the role doesn't exist yet, it is created automatically
	 * @param user Existing user
	 * @param roleName Role name
	 */
	public void addRole(AuthUser user, String roleName) {
		AuthRoleRepo roleRepo = JuDbUtils.getJpaRepository(this.em, AuthRoleRepo.class);
		AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.em, AuthUserRepo.class);
		
		// Check if the role exists
		AuthRole role = roleRepo.getByName(roleName);
		if (role == null) {
			role = new AuthRole();
			role.setName(roleName);
			this.em.persist(role);
		}
		
		// Check if the role has already been assigned to the user
		if (roleRepo.getByNameAndUsersId(roleName, user.getId()) == null) {
			// Role hasn't been assigned, so do it
			user.addRole(role);
			userRepo.save(user);
			roleRepo.save(role);
		}
	}
	
	/**
	 * Removes the specified role from the User.
	 * <p>
	 * If the user doesn't have the role or one of both doesn't exist, nothing is done.
	 * @param user
	 * @param roleName
	 */
	public void removeRole(AuthUser user, String roleName) {
		AuthRoleRepo roleRepo = JuDbUtils.getJpaRepository(this.em, AuthRoleRepo.class);
		AuthUserRepo userRepo = JuDbUtils.getJpaRepository(this.em, AuthUserRepo.class);
		
		// Check if the role exists
		AuthRole role = roleRepo.getByName(roleName);
		if (role != null) {
			if (roleRepo.getByNameAndUsersId(roleName, user.getId()) != null) {
				// User has role assigned, so remove it
				user.removeRole(role);
				roleRepo.save(role);
				userRepo.save(user);
			}
		}
	}
}
