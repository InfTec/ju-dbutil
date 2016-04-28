package ch.inftec.ju.db;

import java.util.List;

import javax.persistence.EntityManagerFactory;

/**
 * Interface that gets a ConnectionCreator instance for a specific connection name.
 * Implementations might provide methods to register connections by name in a 
 * central class.
 * @author tgdmemae
 *
 */
public interface DbConnectionFactory {
	/**
	 * Gets an array containing the available connection names.
	 * @param flags List of flags. If null, all connections are returned. Otherwise,
	 * only connections that have all specified flags set are returned.
	 * @return Array of connection names
	 */
	public List<String> getAvailableConnections(String... flags);
	
	/**
	 * Opens a DbConnection instance with the specified name. Make sure
	 * to close the connection after use. It's recommended to use it in a
	 * try-block.
	 * @param name Connection name
	 * @return DbConnection instance
	 */
	public DbConnection openDbConnection(String name);
	
	/**
	 * Gets an EntityManagerFactory instance for the specified connection name.
	 * <p>
	 * This can be used if we want a third party frameword to handle EntityManager instances.
	 * @param name Connection name
	 * @return EntityManagerFactory for the specified connection
	 */
	public EntityManagerFactory getEntityManagerFactory(String name);
	
	/**
	 * Closes the factory. Should be called when the resource isn't needed anymore.
	 */
	public void close();
}
