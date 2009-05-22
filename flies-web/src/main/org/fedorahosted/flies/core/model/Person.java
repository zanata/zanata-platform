package org.fedorahosted.flies.core.model;

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
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
public class Person extends AbstractFliesEntity implements Serializable {

	private String name;
	private Account account;

	private String email;

	private List<Project> maintainerProjects;

	private Set<Tribe> tribeChiefs;
	private Set<Tribe> tribeMemberships;
	private Set<Tribe> tribeLeaderships;

	private Set<Community> communityOwnerships;
	private Set<Community> communityOfficerships;
	private Set<Community> communityMemberships;
	
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
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
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
	public String getEmail() {
		return email;
	}

	@ManyToMany
	@JoinTable(name = "Project_Maintainer", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "projectId"))
	public List<Project> getMaintainerProjects() {
		return maintainerProjects;
	}

	public void setMaintainerProjects(List<Project> maintainerProjects) {
		this.maintainerProjects = maintainerProjects;
	}

	@ManyToMany
	@JoinTable(name = "Tribe_Member", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "tribeId"))
	public Set<Tribe> getTribeMemberships() {
		return tribeMemberships;
	}

	public void setTribeMemberships(Set<Tribe> tribeMemberships) {
		this.tribeMemberships = tribeMemberships;
	}
	
	@ManyToMany
	@JoinTable(name = "Tribe_Leader", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "tribeId"))
	public Set<Tribe> getTribeLeaderships() {
		return tribeLeaderships;
	}
	
	public void setTribeLeaderships(Set<Tribe> tribeLeaderships) {
		this.tribeLeaderships = tribeLeaderships;
	}
	
	
	@OneToMany(mappedBy = "chief")
	public Set<Tribe> getTribeChiefs() {
		return tribeChiefs;
	}
	
	public void setTribeChiefs(Set<Tribe> tribeChiefs) {
		this.tribeChiefs = tribeChiefs;
	}
	
	@OneToMany(mappedBy = "owner")
	public Set<Community> getCommunityOwnerships() {
		return communityOwnerships;
	}
	
	public void setCommunityOwnerships(Set<Community> communityOwnerships) {
		this.communityOwnerships = communityOwnerships;
	}

	@ManyToMany
	@JoinTable(name = "Community_Officer", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "communityId"))
	public Set<Community> getCommunityOfficerships() {
		return communityOfficerships;
	}
	
	public void setCommunityOfficerships(Set<Community> communityOfficerships) {
		this.communityOfficerships = communityOfficerships;
	}
	
	@ManyToMany
	@JoinTable(name = "Community_Member", joinColumns = @JoinColumn(name = "personId"), inverseJoinColumns = @JoinColumn(name = "communityId"))
	public Set<Community> getCommunityMemberships() {
		return communityMemberships;
	}
	
	public void setCommunityMemberships(Set<Community> communityMemberships) {
		this.communityMemberships = communityMemberships;
	}
}
