package ch.inftec.ju.db.auth;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionException;

import ch.inftec.ju.db.auth.UnknownUserHandler.NewUserInfo;
import ch.inftec.ju.db.auth.entity.AuthRole;
import ch.inftec.ju.db.auth.entity.AuthUser;

/**
 * Custom implementation of the Spring UserDetailsService.
 * <p>
 * Can be used with the DaoAuthenticationProvider.
 * <p>
 * To use this service, Spring dependencies must be available:
 * <ul>
 *   <li>org.springframework.data:spring-data-jpa</li>
 *   <li>org.springframework.security:spring-security-core</li>
 *   <li>org.springframework.security:spring-security-config (when configuring by XML)</li>
 * </ul>
 * <p>
 * The class must run in a Swing Container that will inject a DbConnection dependency.
 * The service will close the connection when it's done with the lookup.
 * <p>
 * An optional UnknownUserHandler implementation can be injected to handle unknown users.
 * @author Martin
 *
 */
public class JuUserDetailsService implements UserDetailsService {
	private Logger logger = LoggerFactory.getLogger(JuUserDetailsService.class);
	
	@Autowired(required=false)
	private UnknownUserHandler unknownUserHandler;
	
	@Autowired
	private AuthenticationEditorModel authenticationEditorModel;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AuthUser authUser = null;
		boolean noDbConn = false;
		try {
			authUser = this.authenticationEditorModel.getUser(username);
		} catch (TransactionException ex) {
			logger.warn(String.format("Couldn't get authentication info for user %s from DB", username), ex);
			noDbConn = true;
		}
		
		if (authUser == null) {
			if (this.unknownUserHandler != null) {
				// Check whether the user should be added
				NewUserInfo newUserInfo = this.unknownUserHandler.handleUser(username);
				if (newUserInfo != null) {
					if (!noDbConn) {
						// Create the user on the DB
						authUser = this.authenticationEditorModel.addUser(username, newUserInfo.getPassword(), newUserInfo.getAuthorities());
					} else {
						// No DB connectivity, so just return user as defined by UnknownUserHandler
						newUserInfo.getAuthorities();
						
						List<GrantedAuthority> grantedAuths = new ArrayList<>();
						for (String role : newUserInfo.getAuthorities()) {
							grantedAuths.add(new SimpleGrantedAuthority(role));
						}
						return new User(username, newUserInfo.getPassword(), grantedAuths);						
					}
				}
			}
			
			if (authUser == null) {
				throw new UsernameNotFoundException("No such user: " + username);
			}
		} else {
			this.authenticationEditorModel.updateLoginCount(authUser);
		}
		
		List<GrantedAuthority> grantedAuths = new ArrayList<>();
		for (AuthRole authRole : authUser.getRoles()) {
			grantedAuths.add(new SimpleGrantedAuthority(authRole.getName()));
		}
		
		User user = new User(username, authUser.getPassword(), grantedAuths);
		
		return user;
	}
}
