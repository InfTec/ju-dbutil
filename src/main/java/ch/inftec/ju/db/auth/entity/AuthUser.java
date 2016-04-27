package ch.inftec.ju.db.auth.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ch.inftec.ju.db.AbstractPersistenceObject;
import ch.inftec.ju.util.JuStringUtils;

/**
 * Entity for a User used for authentication.
 * <p>
 * A user can belong to 0-n AuthRoles.
 * @author Martin
 *
 */
@Entity
public class AuthUser extends AbstractPersistenceObject implements Comparable<AuthUser> {
	@Id
	@GeneratedValue
	private Long id;

	@Column(unique=true, nullable=false)
	private String name;
	
	private String password;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastLogin;
	
	public Date getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Date lastLogin) {
		this.lastLogin = lastLogin;
	}

	public Integer getLoginCount() {
		return loginCount;
	}

	public void setLoginCount(Integer loginCount) {
		this.loginCount = loginCount;
	}

	private Integer loginCount;
	
	@ManyToMany(fetch = FetchType.EAGER)
	private Set<AuthRole> roles = new HashSet<>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}
	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns a read-only copy of this users's roles. To modify roles, use the addRole and removeRole methods.
	 * 
	 * @return
	 */
	public Set<AuthRole> getRoles() {
		return new TreeSet<>(this.roles);
	}
	
	public void addRole(AuthRole role) {
		this.roles.add(role);
		role.users.add(this);
	}

	public void removeRole(AuthRole role) {
		this.roles.remove(role);
		role.users.remove(this);
	}

	@Override
	public int compareTo(AuthUser o) {
		return this.getName().compareTo(o.getName());
	}
	
	@Override
	public String toString() {
		return JuStringUtils.toString(this, "name", this.getName());
	}
}
