package ch.inftec.ju.testing.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.inftec.ju.db.JuEmfUtil;
import ch.inftec.ju.util.JuUtils;
import ch.inftec.ju.util.PropertyChain;

/**
 * Helper class to create EmfWork instance based on property files or
 * system properties.
 * <p>
 * Make sure to call the EmfWork.close method when the resource is no longer needed.
 * @author Martin
 *
 */
public final class EmfUtilProvider {
	private Logger logger = LoggerFactory.getLogger(EmfUtilProvider.class);

	/**
	 * Creates a JuEmfUtil instance for the specified persistence unit and profile.
	 * @param persistenceUnitName PersistenceUnitName
	 * @param profile Profile name. If null, the value of the property <code>ju-dbutil-test.profile</code> will be used
	 * @return JuEmfUtil instance
	 */
	public JuEmfUtil createEmfUtil(String persistenceUnitName, String profile) {
		logger.debug("Creating JuEmfUtil for PU {} and profile {}", persistenceUnitName, profile);
		
		PropertyChain pc = JuUtils.getJuPropertyChain();
		
		String profileName = profile != null ? profile : pc.get("ju-dbutil-test.profile", true);
		String prefix = "ju-dbutil-test." + profileName;
		
		String connectionUrl = pc.get(prefix + ".connectionUrl", false);
		String user = pc.get(prefix + ".user", false);
		String password = pc.get(prefix + ".password", false);
		
		JuEmfUtil emfUtil = JuEmfUtil.create()
				.persistenceUnitName(persistenceUnitName)
				.connectionUrl(connectionUrl)
				.user(user)
				.password(password)
				.build();
		
		return emfUtil;
	}
}
