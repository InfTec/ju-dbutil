package ch.inftec.ju.db;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ch.inftec.ju.util.JuStringUtils;

/**
 * Convenience class of a DriverManagerDataSource that has a ConnectionInfo
 * property to set parameters using a ConnectionInfo instance.
 * @author Martin
 *
 */
public class ConnectionInfoDriverManagerDataSource extends DriverManagerDataSource {
	private ConnectionInfo connectionInfo;
	
	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.setUrl(connectionInfo.getConnectionString());
		this.setUsername(connectionInfo.getUserName());
		this.setPassword(connectionInfo.getPassword());
		
		this.connectionInfo = connectionInfo;
	}
	
	public ConnectionInfo getConnectionInfo() {
		return this.connectionInfo;
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "connectionInfoName", 
				this.connectionInfo == null ? null : ObjectUtils.toString(this.connectionInfo.getName()));
	}
}
