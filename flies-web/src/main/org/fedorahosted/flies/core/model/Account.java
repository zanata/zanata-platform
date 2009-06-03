package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.Set;
import java.security.MessageDigest;

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
import org.hibernate.validator.Length;
import org.jboss.seam.annotations.security.management.UserEnabled;
import org.jboss.seam.annotations.security.management.UserFirstName;
import org.jboss.seam.annotations.security.management.UserPassword;
import org.jboss.seam.annotations.security.management.UserPrincipal;
import org.jboss.seam.annotations.security.management.UserRoles;
import org.jboss.seam.security.management.PasswordHash;
import org.jboss.seam.util.Hex;

@Entity
public class Account extends AbstractFliesEntity implements Serializable {

	private String username;
	private String passwordHash;
	private boolean enabled;
        private String apiKey;

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

        @Length(min=32,max=32)
        public String getApiKey() {
        	return apiKey;
        }

        public void setApiKey(String key) {
               	try {
            		MessageDigest md5 = MessageDigest.getInstance("MD5");
            		md5.reset();
            		this.apiKey = new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
        	} catch (Exception exc) {
            	throw new RuntimeException(exc);
        	}
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (enabled ? 1231 : 1237);
		result = prime * result
				+ ((passwordHash == null) ? 0 : passwordHash.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Account other = (Account) obj;
		if (enabled != other.enabled)
			return false;
		if (passwordHash == null) {
			if (other.passwordHash != null)
				return false;
		} else if (!passwordHash.equals(other.passwordHash))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	
	
}
