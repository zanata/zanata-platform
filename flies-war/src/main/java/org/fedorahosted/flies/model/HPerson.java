package org.fedorahosted.flies.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;

@Entity
public class HPerson extends AbstractFliesEntity implements Serializable {

	private String name;
	private HAccount account;

	private String email;

	private List<HProject> maintainerProjects;

	private Set<HTribe> tribeChiefs;
	private Set<HTribe> tribeMemberships;
	private Set<HTribe> tribeLeaderships;

	private Set<HCommunity> communityOwnerships;
	private Set<HCommunity> communityOfficerships;
	private Set<HCommunity> communityMemberships;
	
	@NotEmpty
	@Length(min=2, max=80)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "accountId")
	public HAccount getAccount() {
		return account;
	}

	public void setAccount(HAccount account) {
		this.account = account;
	}

	@Transient
	public boolean hasAccount() {
		return account != null;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Email
	@NotEmpty
	@NaturalId(mutable=true)
	public String getEmail() {
		return email;
	}

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "HProject_Maintainer", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "projectId"))
	public List<HProject> getMaintainerProjects() {
		return maintainerProjects;
	}

	public void setMaintainerProjects(List<HProject> maintainerProjects) {
		this.maintainerProjects = maintainerProjects;
	}

	@ManyToMany
	@JoinTable(name = "HTribe_Member", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "tribeId"))
	public Set<HTribe> getTribeMemberships() {
		return tribeMemberships;
	}

	public void setTribeMemberships(Set<HTribe> tribeMemberships) {
		this.tribeMemberships = tribeMemberships;
	}
	
	@ManyToMany
	@JoinTable(name = "HTribe_Leader", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "tribeId"))
	public Set<HTribe> getTribeLeaderships() {
		return tribeLeaderships;
	}
	
	public void setTribeLeaderships(Set<HTribe> tribeLeaderships) {
		this.tribeLeaderships = tribeLeaderships;
	}
	
	
	@OneToMany(mappedBy = "chief")
	public Set<HTribe> getTribeChiefs() {
		return tribeChiefs;
	}
	
	public void setTribeChiefs(Set<HTribe> tribeChiefs) {
		this.tribeChiefs = tribeChiefs;
	}
	
	@OneToMany(mappedBy = "owner")
	public Set<HCommunity> getCommunityOwnerships() {
		return communityOwnerships;
	}
	
	public void setCommunityOwnerships(Set<HCommunity> communityOwnerships) {
		this.communityOwnerships = communityOwnerships;
	}

	@ManyToMany
	@JoinTable(name = "HCommunity_Officer", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "communityId"))
	public Set<HCommunity> getCommunityOfficerships() {
		return communityOfficerships;
	}
	
	public void setCommunityOfficerships(Set<HCommunity> communityOfficerships) {
		this.communityOfficerships = communityOfficerships;
	}
	
	@ManyToMany
	@JoinTable(name = "HCommunity_Member", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "communityId"))
	public Set<HCommunity> getCommunityMemberships() {
		return communityMemberships;
	}
	
	public void setCommunityMemberships(Set<HCommunity> communityMemberships) {
		this.communityMemberships = communityMemberships;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime
				* result
				+ ((maintainerProjects == null) ? 0 : maintainerProjects
						.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		HPerson other = (HPerson) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (maintainerProjects == null) {
			if (other.maintainerProjects != null)
				return false;
		} else if (!maintainerProjects.equals(other.maintainerProjects))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	@Override
	public String toString() {
		return super.toString()+"[name="+name+"]";
	}
	
	
	
}
