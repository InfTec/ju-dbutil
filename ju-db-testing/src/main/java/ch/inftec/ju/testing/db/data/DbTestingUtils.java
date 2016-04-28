package ch.inftec.ju.testing.db.data;

import javax.persistence.EntityManagerFactory;

import ch.inftec.ju.db.DbConnectionFactory;
import ch.inftec.ju.db.DbConnectionFactoryLoader;

/**
 * Class containing methods to facilitate DB testing.
 * @author Martin
 *
 */
public class DbTestingUtils {
	/**
	 * Gets an instance of DbConnectionFactory that connects to a Derby Test DB.
	 * <p>
	 * The factory contains one connection named 'Derby InMemory-DB'
	 * @return DbConnectionFactory instance
	 */
	public static DbConnectionFactory getDerbyTestDbConnectionFactory() {
		return DbConnectionFactoryLoader.createInstance("/META-INF/ju-testing_persistence.xml");
	}
	
	/**
	 * Gets an instance of an EntityManagerFactory to a Derby InMemory Test DB.
	 * @return EntityManagerFactory to Derby in Memory test DB
	 */
	public static EntityManagerFactory getDerbyTestDbEntityManagerFactory() {
		return DbTestingUtils.getDerbyTestDbConnectionFactory().getEntityManagerFactory("Derby InMemory-DB");
	}
}
