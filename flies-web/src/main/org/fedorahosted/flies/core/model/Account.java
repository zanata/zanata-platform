package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.NotNull;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserFirstName;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.security.management.PasswordHash;

@Entity
public class Account extends AbstractFliesEntity implements Serializable {

	private String username;
	private String passwordHash;
	private boolean enabled;

	private Person person;
	private Set<AccountRole> roles;
	
	@OneToOne(mappedBy = "account")
	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	@NaturalId
	@UserPrincipal
	public String getUsername() {
		return username;
	}

	@Transient
	public boolean isPersonAccount(){
		return person != null;
	}
	
	public void setUsername(String username) {
		this.username = username;
	}

	@UserPassword(hash = PasswordHash.ALGORITHM_MD5)
	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	@UserEnabled
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@UserRoles
	@ManyToMany(targetEntity = AccountRole.class)
	@JoinTable(name = "AccountMembership", joinColumns = @JoinColumn(name = "accountId"), inverseJoinColumns = @JoinColumn(name = "memberOf"))
	public Set<AccountRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<AccountRole> roles) {
		this.roles = roles;
	}

}
