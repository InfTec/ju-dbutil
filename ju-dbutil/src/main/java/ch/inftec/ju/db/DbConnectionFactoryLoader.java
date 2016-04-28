package ch.inftec.ju.db;

import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.xml.XPathGetter;
import ch.inftec.ju.util.xml.XmlUtils;

/**
 * Helper class to load connections into a DbConnectionFactory instance.
 * @author tgdmemae
 *
 */
public class DbConnectionFactoryLoader {
	Logger log = LoggerFactory.getLogger(DbConnectionFactoryLoader.class);
	
	/**
	 * Make constructor private.
	 */
	private DbConnectionFactoryLoader() {		
	}
	
	/**
	 * Creates a new instance of a DbConnectionFactory. The factory loads the persistence.xml file
	 * which must be found at '/META-INF/persistence.xml'.
	 * @return DbConnectionFactory instance
	 */
	public static DbConnectionFactory createInstance() {
		return DbConnectionFactoryLoader.createInstance("/META-INF/persistence.xml");
	}
	
	/**
	 * Creates a new instance of a DbConnectionFactory using the specified resource.xml file
	 * @param persistenceXmlPath Path to resource.xml file, e.g. /META-INF/persistence.xml
	 * @return DbConnectionFactory intance
	 */
	public static DbConnectionFactory createInstance(String persistenceXmlPath) {
		try {
			return new DbConnectionFactoryLoader().loadFromXml(persistenceXmlPath);
		} catch (Exception ex) {
			throw new JuRuntimeException("Couldn't create DbConnectionFactory instance", ex);
		}
	}
	
	/**
	 * Loads a new ConnectionFactory from the specified XML file. The XML is a standard JPA
	 * persistence.xml file that may contain JU specific properties.
	 * <p>
	 * Additionally, some JU DbUtil specific properties are supported:
	 * <ul>
	 *   <li>ch.inftec.ju.flags: Comma separated list of flags for the connection, e.g. productive,admin. Can be used
	 *       to get available connections by flags using the DbConnectionFactory</li>
	 *   <li>ch.inftec.ju.schemaName: Explicit Schema name. Currently, this is only used by DbUnit to avoid
	 *       AmbigiousTableNameException on Oracle DBs</li>
	 * </ul>
	 * @param resourceXmlPath Path to the persistence.xml file
	 * @return ConnectionFactory instance containing the connections defined in the XML
	 * @throws IllegalArgumentException If the XML cannot be processed
	 */
	private DbConnectionFactory loadFromXml(String resourceXmlPath) {
		log.info("Loading DB connections from XML: " + resourceXmlPath);
		
		try {
			URL persistenceXmlUrl = DbConnectionFactoryLoader.class.getResource(resourceXmlPath);
			
			DbConnectionFactoryImpl factory = new DbConnectionFactoryImpl(resourceXmlPath);
			
			Document doc = XmlUtils.loadXml(persistenceXmlUrl, null);
			XPathGetter xg = new XPathGetter(doc);
			
			for (XPathGetter xgConn : xg.getGetters("persistence/persistence-unit")) {
				String name = xgConn.getSingle("@name");
				String flagString = xgConn.getSingle("properties/property[@name='ch.inftec.ju.flags']/@value");
				String schemaName = xgConn.getSingle("properties/property[@name='ch.inftec.ju.schemaName']/@value");
				
				String flags[] = flagString != null
						? StringUtils.stripAll(StringUtils.split(flagString, ','))
						: new String[0];
				
				factory.addDbConnection(name, schemaName, flags);
			}
			
			return factory;
		} catch (Exception ex) {
			throw new IllegalArgumentException("Couldn't load connections from XML: " + resourceXmlPath, ex);
		}		
	}
}
