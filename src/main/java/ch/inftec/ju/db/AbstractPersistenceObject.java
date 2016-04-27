package ch.inftec.ju.db;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;

import javax.persistence.Id;
import javax.persistence.Transient;

import ch.inftec.ju.util.JuRuntimeException;
import ch.inftec.ju.util.ReflectUtils;

/**
 * Base class for JPA entity beans.
 * <p>
 * Overrides the hashCode and equals method to provide better support for
 * lists and sets.
 * <p>
 * The entity bean must have exactly one @Id annotated field that must be non-null (and not
 * change) when the hashCode or equals method is first accessed. This means that the JPA implementation
 * needs to assign a (table) unique ID that won't change when the entity is persisted. And it must be
 * persisted before it is used in any hashCode based container (like List, Set, ...) that are used to
 * model JPA relations.
 * @author Martin
 *
 */
public abstract class AbstractPersistenceObject implements Serializable {
	@Transient
	private Integer hashCode;
	@Transient
	private Object idFieldValue;
	
	@Override
	public int hashCode() {
		if (this.hashCode == null) {
			// Get the primary key annotated with ID
			List<Field> fields = ReflectUtils.getDeclaredFieldsByAnnotation(this.getClass(), Id.class);
			
			if (fields.size() != 1) {
				throw new JuRuntimeException("Expected exactly one @Id annotaded field");
			}
			
			// Get the value of the ID field
			this.idFieldValue = ReflectUtils.getFieldValue(this, fields.get(0));
			if (this.idFieldValue == null) {
				throw new JuRuntimeException("Id of EntityBean must not be null when first hashCode is computed");
			}
			
			// Compute the hashcode
			this.hashCode = this.idFieldValue.hashCode();
		}
		
		return this.hashCode;
	};
	
	@Override
	public boolean equals(Object obj) {
		// We only consider exact class matches as potentially equal
		if (obj != null && obj.getClass() == this.getClass()) {
			// First, check if the hashCode is equal
			if (this.hashCode() == obj.hashCode()) {
				AbstractPersistenceObject persObj = (AbstractPersistenceObject)obj;
				return this.idFieldValue.equals(persObj.idFieldValue);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
