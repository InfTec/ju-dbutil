package ch.inftec.ju.util.persistable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class TestObject {
	@Id
	@GeneratedValue
	private Long id;
	
	private String text;
	
	private Long value;

	public TestObject() {		
	}
	
	public TestObject(String text) {
		this(text, null);
	}
	
	public TestObject(String text, Long value) {
		this.text = text;
		this.value = value;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
}
