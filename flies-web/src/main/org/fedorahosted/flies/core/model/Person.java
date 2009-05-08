package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
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
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
public class Person extends AbstractFliesEntity implements Serializable {

	private String name;
	private Account account;

	private String email;

	private String personId;

	// from Damned Lies:
	private String imageUrl;
	private String ircNick;
	private String webpageUrl;

	private List<Project> maintainerProjects;

	private Set<Tribe> tribeChiefs;
	private Set<Tribe> tribeMemberships;
	private Set<Tribe> tribeLeaderships;

	private Set<Community> communityOwnerships;
	private Set<Community> communityOfficerships;
	private Set<Community> communityMemberships;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	@NaturalId
	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	@OneToOne(mappedBy = "person")
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

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getIrcNick() {
		return ircNick;
	}

	public void setIrcNick(String ircNick) {
		this.ircNick = ircNick;
	}

	public String getWebpageUrl() {
		return webpageUrl;
	}

	public void setWebpageUrl(String webpageUrl) {
		this.webpageUrl = webpageUrl;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Email
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
