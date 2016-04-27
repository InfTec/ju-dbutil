package ch.inftec.ju.db.auth;

import java.util.List;

/**
 * Interface that is used to evaluate all available Roles a User can have.
 * @author Martin
 *
 */
public interface RoleProvider {
	/**
	 * Gets a list of all available roles.
	 * @return List of available roles
	 */
	List<String> getAvailableRoles();
}
