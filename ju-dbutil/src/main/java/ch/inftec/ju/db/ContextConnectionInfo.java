package ch.inftec.ju.db;

import java.awt.Image;

import javax.swing.Icon;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.util.JuRuntimeException;

/**
 * Implementation of the ConnectionInfo interface that reads its info
 * from the ConnectionInfoContextHolder class.
 * <p>
 * Note that this wrapper will throw a runtime exception if no ConnectionInfo is
 * set in the ContextHolder.
 * @author Martin
 *
 */
public class ContextConnectionInfo implements ConnectionInfo {
	@Autowired
	private ConnectionInfoContextHolder contextHolder;
	
	private ConnectionInfo getConnectionInfo() {
		if (this.contextHolder.getConnectionInfo() == null) {
			throw new JuRuntimeException("No ConnectionInfo set in ContextHolder");
		}
		return this.contextHolder.getConnectionInfo();
	}
	
	@Override
	public int compareTo(ConnectionInfo o) {
		return ObjectUtils.compare(this.getName(), o == null ? null : o.getName());
	}

	@Override
	public String getName() {
		return this.getConnectionInfo().getName();
	}

	@Override
	public String getConnectionString() {
		return this.getConnectionInfo().getConnectionString();
	}

	@Override
	public String getUserName() {
		return this.getConnectionInfo().getUserName();
	}

	@Override
	public String getPassword() {
		return this.getConnectionInfo().getPassword();
	}

	@Override
	public String getSchema() {
		return this.getConnectionInfo().getSchema();
	}

	@Override
	public Icon getIcon() {
		return this.getConnectionInfo().getIcon();
	}

	@Override
	public Image getImage() {
		return this.getConnectionInfo().getImage();
	}

}
