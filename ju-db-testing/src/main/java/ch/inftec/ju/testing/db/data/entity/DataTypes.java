package ch.inftec.ju.testing.db.data.entity;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import ch.inftec.ju.db.AbstractPersistenceObject;
import ch.inftec.ju.util.JuStringUtils;

@Entity
public class DataTypes extends AbstractPersistenceObject {
	@Id
	@GeneratedValue
	private Long id;

	private Integer intNumber;
	
	private Long bigIntNumber;
	
	private String varcharText;
	
	//@Lob
	private String clobText;
	
	//@Temporal
	private Date dateField;
	
	private Date timeField;
	
	private Date timeStampField;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getIntNumber() {
		return intNumber;
	}

	public void setIntNumber(Integer intNumber) {
		this.intNumber = intNumber;
	}

	public Long getBigIntNumber() {
		return bigIntNumber;
	}

	public void setBigIntNumber(Long bigIntNumber) {
		this.bigIntNumber = bigIntNumber;
	}

	public String getVarcharText() {
		return varcharText;
	}

	public void setVarcharText(String varcharText) {
		this.varcharText = varcharText;
	}

	public String getClobText() {
		return clobText;
	}

	public void setClobText(String clobText) {
		this.clobText = clobText;
	}

	public Date getDateField() {
		return dateField;
	}

	public void setDateField(Date dateField) {
		this.dateField = dateField;
	}

	public Date getTimeField() {
		return timeField;
	}

	public void setTimeField(Date timeField) {
		this.timeField = timeField;
	}

	public Date getTimeStampField() {
		return timeStampField;
	}

	public void setTimeStampField(Date timeStampField) {
		this.timeStampField = timeStampField;
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "id", this.getId());
	}
}
