package ch.inftec.ju.db;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContextHolder class used by the ConnectionInfoRoutingDataSource to set the
 * ConnectionInfo that should be used for the current Thread.
 * <p>
 * Note that the ConnectionInfo will only apply to the NEXT Spring transaction.
 * We cannot change the ConnectionInfo for the currently running transaction.
 * @author Martin
 *
 */
public class ConnectionInfoContextHolder {
	final Logger logger = LoggerFactory.getLogger(ConnectionInfoContextHolder.class);
	
	private final ThreadLocal<ConnectionInfo> contextHolder = new ThreadLocal<>();
	private Set<ConnectionInfo> availableConnectionInfos = new LinkedHashSet<>();
	
	/**
	 * Sets the ConnectionInfo to be used by the following database interactions / transaction.
	 * <p>
	 * If we set the ConnectionInfo within a transaction, it will be applied to the next
	 * Transaction that is started - or to any database interaction without a transaction.
	 * @param connectionInfo New ConnectionInfo
	 */
	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		logger.debug("Setting ConnectionInfo: " + connectionInfo);
		contextHolder.set(connectionInfo);
	}

	/**
	 * Sets the ConnectionInfo to be used by its name. If it doesn't exist, a
	 * runtime exception is thrown
	 * <p>
	 * If we set the ConnectionInfo within a transaction, it will be applied to the next
	 * Transaction that is started - or to any database interaction without a transaction.
	 * @param connectionInfoName New ConnectionInfo name
	 */
	public void setConnectionInfoByName(String connectionInfoName) {
		for (ConnectionInfo connectionInfo : availableConnectionInfos) {
			if (connectionInfoName.equals(connectionInfo.getName())) {
				setConnectionInfo(connectionInfo);
				return;
			}
		}
		
		throw new JuDbException("No ConnectionInfo available by the name " + connectionInfoName);
	}
	
	public ConnectionInfo getConnectionInfo() {
		return contextHolder.get();
	}

	/**
	 * Clears the ConnectionInfo, meaning the default ConnectionInfo will be used.
	 */
	public void clearConnectionInfo() {
		contextHolder.remove();
	}
	
	void setAvailableConnectionInfos(Set<ConnectionInfo> availableConnectionInfos) {
		this.availableConnectionInfos = availableConnectionInfos; 
	}
	
	/**
	 * Gets a set of all available ConnectionInfo instances.
	 * @return Set of available ConnectionInfo instances
	 */
	public Set<ConnectionInfo> getAvailableConnectionInfos() {
		return availableConnectionInfos;
	}
	
	/**
	 * Checks if the ConnectionInfo with the specified name is available.
	 * @param name Name of the ConnectionInfo
	 * @return True if the ConnectionInfo exists, false otherwise
	 */
	public boolean hasConnectionInfo(String name) {
		for (ConnectionInfo connectionInfo : getAvailableConnectionInfos()) {
			if (connectionInfo.getName().equals(name)) return true;
		}
		
		return false;
	}
}
