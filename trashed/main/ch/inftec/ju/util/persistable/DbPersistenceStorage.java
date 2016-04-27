package ch.inftec.ju.util.persistable;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import ch.inftec.ju.db.ConnectionInfo;
import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.persistable.GenericMemento.MementoAttribute;
import ch.inftec.ju.util.persistable.GenericMementoUtils.GenericMementoBuilder;

/**
 * Implementation of a DB memento storage. Uses JPA and the entities MementoObject
 * and AttributeObject to persist the memento.
 * <br>
 * Use the persistence.xml file to configure the database and tables if necessary.
 * <p>
 * The following attributes are stored in special DB columns if they are present in the
 * Memento:
 * <ul>
 * 	<li><b>.connectionName</b>: ATTR_CONNECTION_NAME. Name of the connection that was involved in the Memento's parent object execution</li>
 *  <li><b>.executionTime</b>: ATTR_EXECUTION_TIME. Execution time of the Memento's parent's execution</li>
 * </ul>
 * There's a constant in DbPersistenceStorage for each of them.
 * @author Martin
 *
 */
public final class DbPersistenceStorage implements MementoStorage {
	@PersistenceContext
	private EntityManager em;
	
	@Autowired
	private ConnectionInfo connectionInfo; 
	
	public static final String ATTR_CONNECTION_NAME = ".connectionName";
	public static final String ATTR_EXECUTION_TIME = ".executionTime";
	
	@Override
	public Long persistMemento(GenericMemento memento, String type) {
		Long id = null;
		
		MementoObject mo = this.createMementoObject(memento, type);
		
		this.em.persist(mo);
		id = mo.getId();

		return id;
	}
	
	@Override
	public GenericMementoItem loadMemento(Long id) {
		MementoObject mo =this.em.find(MementoObject.class, id);
		
		GenericMementoBuilder builder = GenericMementoUtils.builder();
		this.buildGenericMemento(mo, builder);
		return GenericMementoUtils.newGenericMementoItem(builder.build(), id, mo.getType());			
	}

	@Override
	public List<GenericMementoItem> loadMementos(int maxCount) {
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Recursively create a MementoObject for the specified GenericMemento.
	 * @param memento GenericMemento
	 * @param type Type of the GenericMemento
	 * @return MementoObject for the GenericMemento that can be persisted
	 * to an EntityManager
	 */
	private MementoObject createMementoObject(GenericMemento memento, String type) {
		MementoObject mo = new MementoObject();
		
		GenericMementoX moX = GenericMementoUtils.asX(memento);
		mo.setDbName(moX.getStringValue(".connectionName"));
		mo.setExecutionTime(moX.getDateValue(".executionTime"));
		
		// Set MetaData
		mo.setType(type);
		
		// Add children
		for (GenericMemento mementoChild : memento.getChildren()) {
			mo.addChild(this.createMementoObject(mementoChild, null));
		}
		
		// Add attributes
		for (MementoAttribute attribute : memento.getAttributes()) {
			AttributeObject attr = new AttributeObject();
			attr.setKey(attribute.getKey());
			attr.setStringValue(attribute.getStringValue());
			attr.setDateValue(attribute.getDateValue());
			attr.setLongValue(attribute.getLongValue());
			
			mo.addAttribute(attr);
		}
		
		return mo;		
	}

	/**
	 * Creates a GenericMemento instance for the specified MementoObject.
	 * @param mo MementoObject as loaded from the DB
	 * @param builder GenericMementoBuilder used to build the memento or null for the root memento
	 * @return GenericMemento instance
	 */
	private void buildGenericMemento(MementoObject mo, GenericMementoBuilder builder) {
		// Add children
		for (MementoObject mementoChild : mo.getChildren()) {
			GenericMementoBuilder childBuilder = builder.newChild();
			this.buildGenericMemento(mementoChild, childBuilder);
			childBuilder.childDone();
		}
		
		// Add attributes
		for (AttributeObject attr : mo.getAttributes()) {
			builder.add(attr);
		}
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(DbPersistenceStorage.class,
				"connectionName", this.connectionInfo.getName());
	}
}
