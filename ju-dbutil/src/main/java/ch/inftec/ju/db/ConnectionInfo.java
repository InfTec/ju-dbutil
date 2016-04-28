package ch.inftec.ju.db;

import java.awt.Image;

import javax.swing.Icon;

/**
 * Interface providing information about a Database connection.
 * <p>
 * ConnectionInfos are considered to be equal if they have an identical name. Implementations
 * must override the hashCode and equals methods accordingly.
 * @author Martin
 *
 */
public interface ConnectionInfo extends Comparable<ConnectionInfo> {
	/**
	 * Gets the (human readable) name for the connection. That is the logic name
	 * that can be displayed in an application and does not have to be relatec to the
	 * connection string or the persistence unit name.
	 * @return Logic name, e.g. 'Test DB'
	 */
	String getName();
	
	/**
	 * Gets the connection String for the DataSource to connect to the Database.
	 * @return Database specific connection String
	 */
	public String getConnectionString();

	/**
	 * Gets the UserName to connect to the database.
	 * @return UserName
	 */
	public String getUserName();
	
	/**
	 * Gets the password used to connect to the database.
	 * @return Password
	 */
	public String getPassword();
	
	/**
	 * Gets the Schema name of the connection.
	 * <p>
	 * NOTE: Currently, this is not used by the EntityManager, just as an information when
	 * using the raw connection
	 * @return Schema name
	 */
	public String getSchema();

	/**
	 * Gets an Icon for the DB connection.
	 * @return Icon or null if none is specified
	 */
	public Icon getIcon();

	/**
	 * Gets an Image for the DB connection.
	 * @return Image or null if none is specified
	 */
	public Image getImage();
}