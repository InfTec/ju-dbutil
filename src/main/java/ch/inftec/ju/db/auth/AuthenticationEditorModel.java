package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import ch.inftec.ju.db.JuDbException;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;
import ch.inftec.ju.db.auth.repo.AuthUserRepo;
import ch.inftec.ju.util.JuCollectionUtils;
import ch.inftec.ju.util.JuRuntimeException;

/**
 * Model to manage users and roles for Authentication Services.
 * <p>
 * This class works with the AuthUser and AuthRole entities.
 * 
 * @author Martin
 *
 */
@Transactional(value="transactionManagerJuAuth")
public class AuthenticationEditorModel {
	@PersistenceContext(unitName="juAuth")
	private EntityManager em;
	
	@Autowired
	private RoleProvider roleProvider;
	
	@Autowired
	private AuthUserRepo userRepo;
	
	@Autowired
	private AuthDao authDao;
	
	/**
	 * Gets a list of all available users.
	 * @return List of users, sorted by the UserName
	 */
	public List<AuthUser> getUsers() {
		return this.userRepo.findAll();
	}
	
	public static interface UserCallback {
		void process(AuthUser user);
	}

	/**
	 * Iterates over all users in a transaction and calls the specified callback function for each user.
	 */
	public void processUsers(UserCallback c) {
		for (AuthUser user : getUsers()) {
			c.process(user);
		}
	}

	/**
	 * Gets all user names.
	 * @return List of user names, sorted alphabetically
	 */
	public List<String> getUserNames() {
		return this.userRepo.findAllNames();
	}
	
	/**
	 * Adds the specified user.
	 * @param userName
	 * @param password
	 */
	public AuthUser addUser(String userName, String password) {
		// Make sure the user doesn't exist yet
		// TODO: Use AssertUtil
		if (this.getUser(userName) != null) {
			throw new JuDbException("User already exists: " + userName);
		}
		if (password == null) {
			throw new JuRuntimeException("Password must not be null");
		}
		
		
		AuthUser newUser = new AuthUser();
		newUser.setName(userName);
		newUser.setPassword(password);
		newUser.setLoginCount(1);
		newUser.setLastLogin(new Date());
		
		this.userRepo.save(newUser);
		
		return newUser;
	}
	
	/**
	 * Delete the specified user
	 * @param userName User name
	 */
	public void deleteUser(String userName) {
		AuthUser user = this.getUser(userName);
		if (user != null) {
			this.userRepo.delete(user);
		}
	}
	
	/**
	 * Adds a new user and assigns the specified roles.
	 * @param userName Username
	 * @param password Password (non-null)
	 * @param roles roles to be assigned
	 * @return Added user
	 */
	public AuthUser addUser(String userName, String password, List<String> roles) {
		AuthUser authUser = addUser(userName, password);
		setRoles(authUser, roles);
		
		return authUser;
	}
	
	/**
	 * Gets the AuthUser object for the specified user name.
	 * @param userName
	 * @return AuthUser instance or null if the user doesn't exist
	 */
	public AuthUser getUser(String userName) {
		return userRepo.getByName(userName);
	}
	
	/**
	 * Gets all roles the specified user has.
	 * @param user
	 * @return List of role names
	 */
	public List<String> getRoles(AuthUser user) {
		this.em.merge(user);
		List<String> roles = new ArrayList<>();
		for (AuthRole role : user.getRoles()) {
			roles.add(role.getName());
		}
		
		return roles;
	}
	
	/**
	 * Gets a list of all available roles.
	 * @return List of available roles
	 */
	public List<String> getAvailableRoles() {
		return this.roleProvider.getAvailableRoles();
	}

	/**
	 * Sets the specified roles for the user.
	 * <p>
	 * This method will remove any roles that the user currently has, but that are not
	 * specified in the roles list.
	 * @param u
	 * @param roles All roles the user should have, including the current roles that
	 * shouldn't be deleted
	 */
	public void setRoles(AuthUser user, List<String> roles) {
		this.em.merge(user);		
		List<String> currentRoles = this.getRoles(user);
		
		for (String role : JuCollectionUtils.emptyForNull(roles)) {
			if (currentRoles.contains(role)) {
				// Just leave the role as is
				currentRoles.remove(role);
				continue;
			} else {
				// Add the new role
				this.authDao.addRole(user, role);
			}
		}
		
		// Remove any remaining role the user had previously
		for (String role : JuCollectionUtils.emptyForNull(currentRoles)) {
			this.authDao.removeRole(user, role);
		}
	}
	
	public void updateLoginCount(AuthUser user) {
		user.setLastLogin(new Date());
		user.setLoginCount(user.getLoginCount() != null ? user.getLoginCount() + 1 : 1);
		this.userRepo.save(user);
	}
}
