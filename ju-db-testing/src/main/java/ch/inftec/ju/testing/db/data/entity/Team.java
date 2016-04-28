package ch.inftec.ju.testing.db.data.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

import ch.inftec.ju.db.AbstractPersistenceObject;

@Entity
public class Team extends AbstractPersistenceObject {
	@Id
	@GeneratedValue
	private Long id;
	
	private String name;
	
	private int ranking;
	
	@Temporal(TemporalType.DATE)
	private Date foundingDate;

	@Version
	private int version;
	
	@ManyToMany
	private Set<Player> players = new HashSet<>();
	
	public Set<Player> getPlayers() {
		return players;
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

	public int getRanking() {
		return ranking;
	}

	public void setRanking(int ranking) {
		this.ranking = ranking;
	}

	public Date getFoundingDate() {
		return foundingDate;
	}

	public void setFoundingDate(Date foundingDate) {
		this.foundingDate = foundingDate;
	}
}
