package ch.inftec.ju.db.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class CustomObject {
	@Id
	@GeneratedValue
	private Long id;
	
	private String text;

	public Long getId() {
		return this.id;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
