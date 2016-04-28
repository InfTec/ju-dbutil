package ch.inftec.ju.db;

import java.awt.Image;
import java.net.URL;

import javax.swing.Icon;

import org.apache.commons.lang3.ObjectUtils;

import ch.inftec.ju.security.JuSecurityUtils;
import ch.inftec.ju.util.GuiUtils;
import ch.inftec.ju.util.JuObjectUtils;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.JuUtils;

/**
 * Class containing connection information to connect to an
 * Oracle database.
 * <p>
 * This implementation can handle encrypted passwords of the form ENC(encryptedPassword) using the default
 * text encryptor returned by JuUtils. The password will be decrypted as soon as set, from that point forward,
 * the getPassword method will always provided the decrypted value.
 * @author tgdmemae
 *
 */
public class ConnectionInfoImpl implements ConnectionInfo {
	private String name;
	private String connectionString;
	private String userName;
	private String password;
	private String schema;
	private Icon icon;
	private Image image;
	
	private String decryptedPassword;
	
	public ConnectionInfoImpl() {
	}
	
	/**
	 * Creates a new connection info object.
	 * @param name Name of the connection
	 * @param connectionString Oracle specific connection string
	 * @param userName User name
	 * @param password Password
	 * @param schema The DB schema to be used. This is not completely integrated in to MyTTS, it is rather a
	 * workaround to be able to use a different DB user than the actual target Schema.
	 * @param passwordProtected If true, all write operations will require the user to confirm the DB password
	 * @param icon Icon representing the database connection
	 * @param image Image representing the database connection
	 */
	public ConnectionInfoImpl(String name, String connectionString, String userName, String password, String schema, boolean passwordProtected, Icon icon, Image image) {
		this.name = name;
		this.connectionString = connectionString;
		this.userName = userName;
		this.password = password;
		this.schema = schema;
		this.icon = icon;
		this.image = image;
		
		this.handleEncryptedPassword();
	}

	private void handleEncryptedPassword() {
		this.decryptedPassword = JuSecurityUtils.decryptTaggedValueIfNecessary(this.password, JuUtils.getDefaultEncryptor());
	}
	
	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getConnectionString() {
		return connectionString;
	}

	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	@Override
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String getPassword() {
		return this.decryptedPassword;
	}

	public void setPassword(String password) {
		this.password = password;
		
		this.handleEncryptedPassword();
	}

	@Override
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	@Override
	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}
	
	public void setIconUrl(URL iconUrl) {
		this.icon = GuiUtils.loadIconResource(iconUrl);
	}

	@Override
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
	
	public void setImageUrl(URL imageUrl) {
		this.image = GuiUtils.loadIconResource(imageUrl).getImage();
	}

	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
	
	@Override
	public int compareTo(ConnectionInfo o) {
		return ObjectUtils.compare(this.getName(), o == null ? null : o.getName());
	}
	
	@Override
	public boolean equals(Object o) {
		return this.compareTo(JuObjectUtils.as(o, ConnectionInfo.class)) == 0;
	}
	
	@Override
	public int hashCode() {
		return this.getName().hashCode();
	}
}
