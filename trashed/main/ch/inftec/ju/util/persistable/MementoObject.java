package ch.inftec.ju.util.persistable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ch.inftec.ju.util.JuStringUtils;

@Entity
public class MementoObject {
	@Id
	@GeneratedValue
	private Long id;

	private String dbName;
	
	private String type;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date executionTime;

	@OneToMany(targetEntity=AttributeObject.class, mappedBy="parent", cascade=CascadeType.ALL)
	private List<AttributeObject> attributes = new ArrayList<>();

	@ManyToOne
	private MementoObject parent;
	
	@OneToMany(targetEntity=MementoObject.class, mappedBy="parent", cascade=CascadeType.ALL)
	private List<MementoObject> children = new ArrayList<>();
	
	public Long getId() {
		return id;
	}
	
	public String getDbName() {
		return dbName;
	}

	public String getType() {
		return type;
	}

	public Date getExecutionTime() {
		return executionTime;
	}

	protected void setId(Long id) {
		this.id = id;
	}

	protected void setDbName(String dbName) {
		this.dbName = dbName;
	}

	protected void setType(String type) {
		this.type = type;
	}

	protected void setExecutionTime(Date executionTime) {
		this.executionTime = executionTime;
	}
	
	public List<AttributeObject> getAttributes() {
		return attributes;
	}

	void addAttribute(AttributeObject attribute) {
		this.attributes.add(attribute);
		attribute.setParent(this);
	}
	
	public MementoObject getParent() {
		return parent;
	}

	public List<MementoObject> getChildren() {
		return children;
	}
	
	void addChild(MementoObject child) {
		this.children.add(child);
		child.parent = this;
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this,
				"id", this.getId(),
				"type", this.getType(),
				"children", this.getChildren());
	}
}
