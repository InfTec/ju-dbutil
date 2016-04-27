package ch.inftec.ju.util.persistable;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ch.inftec.ju.util.JuStringUtils;
import ch.inftec.ju.util.persistable.GenericMemento.MementoAttribute;

@Entity
public class AttributeObject implements MementoAttribute {
	@Id
	@GeneratedValue
	private Long id;

	private String key;
	
	private String stringValue;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date dateValue;
	
	private Long longValue;
	
	@ManyToOne
	private MementoObject parent;

	public Long getId() {
		return id;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getStringValue() {
		return stringValue;
	}

	@Override
	public Date getDateValue() {
		return dateValue;
	}

	@Override
	public Long getLongValue() {
		return longValue;
	}

	public MementoObject getParent() {
		return parent;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	protected void setKey(String key) {
		this.key = key;
	}

	protected void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	protected void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	protected void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	void setParent(MementoObject parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(AttributeObject.class,
				"id", this.getId(),
				"key", this.getKey(),
				"stringValue", this.getStringValue(),
				"longValue", this.getLongValue(),
				"dateValue", this.getDateValue());
	}
}
