package ch.inftec.ju.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang3.StringUtils;

import ch.inftec.ju.util.JuCollectionUtils;

/**
 * Implementation of the DbConnectionFactory interface to get DbConnection instances.
 * @author tgdmemae
 *
 */
@Deprecated
class DbConnectionFactoryImpl implements DbConnectionFactory {	
	/**
	 * HashMap containing the connection's flags. The linked hash map makes sure the
	 * order is preserved.
	 */
	private LinkedHashMap<String, String[]> flags = new LinkedHashMap<>();
	
	/**
	 * Contains the SchemaNames for the connections.
	 */
	private Map<String, String> schemaNames = new HashMap<>();
	
	/**
	 * Hashtable containing the connection's factories.
	 */
	private Hashtable<String, EntityManagerFactory> factories = new Hashtable<>();
	
	private final String persistenceXmlPath;
	
	public DbConnectionFactoryImpl(String persistenceXmlPath) {
		this.persistenceXmlPath = persistenceXmlPath;
		
	}
	
	@Override
	public EntityManagerFactory getEntityManagerFactory(String name) {
		if (!this.factories.containsKey(name)) {
			Properties props = new Properties();
			
			// EclipseLink doesn't seem to like the '/' at the start of a path, so we'll strip it
			String persistencePath = this.persistenceXmlPath;
			if (persistencePath.startsWith("/")) persistencePath = persistencePath.substring(1);
			
			if (1 == 1) throw new UnsupportedOperationException("Hibernate Refactoring");
			//props.setProperty(PersistenceUnitProperties.ECLIPSELINK_PERSISTENCE_XML, persistencePath);
			this.factories.put(name, Persistence.createEntityManagerFactory(name, props)); 
		}
		
		return this.factories.get(name);
	}

	@Override
	public List<String> getAvailableConnections(String... flags) {
		ArrayList<String> connections = new ArrayList<>();
		
		for (String name : this.flags.keySet()) {			
			if (flags.length > 0) {
				String creatorFlags[] = this.flags.get(name);
				
				if (!JuCollectionUtils.isSubsetOf(flags, creatorFlags)) continue;
			}
			
			connections.add(name);
		}
		
		return Collections.unmodifiableList(connections);
	}

	@Override
	public DbConnection openDbConnection(String name) {
		return null;
//		EntityManagerFactory emf = this.getEntityManagerFactory(name);
//		return new DbConnectionImpl(name, this.schemaNames.get(name), emf);
	}

	/**
	 * Adds the specified connection to the factory.
	 * @param name Connection name
	 * @param flags Flags of the connection, used by the getAvailableConnections method to filters
	 */
	public void addDbConnection(String name, String schemaName, String... flags) {
		if (this.flags.containsKey(name)) {
			throw new IllegalArgumentException("Factory already contains a connection with the name: " + name);
		}
		
		this.flags.put(name, flags);
		this.schemaNames.put(name, StringUtils.trimToNull(schemaName));
	}

	@Override
	public void close() {
		Iterator<EntityManagerFactory> factoriesIterator = this.factories.values().iterator();
		while (factoriesIterator.hasNext()) {
			factoriesIterator.next().close();
			factoriesIterator.remove();
		}
	}
}
