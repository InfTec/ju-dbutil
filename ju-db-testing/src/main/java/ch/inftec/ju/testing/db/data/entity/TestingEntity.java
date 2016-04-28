package ch.inftec.ju.testing.db.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import ch.inftec.ju.db.AbstractPersistenceObject;
import ch.inftec.ju.util.JuStringUtils;

@Entity
public class TestingEntity extends AbstractPersistenceObject {
	@Id
	@GeneratedValue
	private Long id;

	private String name;

	public TestingEntity() {}
	public TestingEntity(String name) {
		this.name = name;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return JuStringUtils.toString(this, "id", this.getId(), "name", this.getName());
	}
}
