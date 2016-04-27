package ch.inftec.ju.db.auth;

import java.util.List;

/**
 * Interface that can be used as a hook up to handle unknown users
 * when using the JuUserDetailsService.
 * <p>
 * For instance, an unknown user could automatically be added to the users
 * with a default (e.g. read-only) role.
 * @author Martin
 *
 */
public interface UnknownUserHandler {
	/**
	 * Handles the unknown user with the specified name.
	 * <p>
	 * If null is returned, the user is rejected. Otherwise, it is
	 * stored as a new user (with the specified password) and the grants
	 * returned by this method.
	 * @param userName Username of the unknown user
	 * @return Null if the user should be rejected or a NewUserInfo instance
	 * containing the information for the new user if it should be added to
	 * the user base
	 */
	public NewUserInfo handleUser(String userName);
	
	/**
	 * Information about a new user that can be returned by a UnknownUserHandler.
	 * @author Martin
	 *
	 */
	public interface NewUserInfo {
		/**
		 * Initial password.
		 * @return
		 */
		public String getPassword();
		
		/**
		 * List of authorities the user should be assigned to.
		 * @return List of authorities for the new user
		 */
		public List<String> getAuthorities();
	}
}
