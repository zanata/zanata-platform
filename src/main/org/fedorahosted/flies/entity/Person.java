package org.fedorahosted.flies.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.validator.Email;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "personId"))
public class Person extends AbstractFliesEntity implements Serializable{

    private String name;
    private Account account;

    private String email;
    
    private String personId;
    
    // from Damned Lies:
    private String imageUrl;
    private String ircNick;
    private String webpageUrl;
    
    private List<Project> maintainerProjects;
    private List<TranslationTeam> teamMemberships;
    
    public String getName() {
		return name;
	}
    
    public void setName(String name) {
		this.name = name;
	}

    @NotNull
    @NotEmpty
    public String getPersonId() {
		return personId;
	}
    
    public void setPersonId(String personId) {
		this.personId = personId;
	}
    
    @OneToOne(mappedBy="person")
    public Account getAccount() {
		return account;
	}
    
    public void setAccount(Account account) {
		this.account = account;
	}
    
    @Transient
    public boolean hasAccount(){
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
    @JoinTable(
            name="Project_Maintainer",
            joinColumns=@JoinColumn(name="personId"),
            inverseJoinColumns=@JoinColumn(name="projectId")
        )
    public List<Project> getMaintainerProjects() {
		return maintainerProjects;
	}
    
    public void setMaintainerProjects(List<Project> maintainerProjects) {
		this.maintainerProjects = maintainerProjects;
	}

    @ManyToMany
    @JoinTable(
            name="TranslationTeam_Member",
            joinColumns=@JoinColumn(name="personId"),
            inverseJoinColumns=@JoinColumn(name="translationTeamId")
        )
    public List<TranslationTeam> getTeamMemberships() {
		return teamMemberships;
	}
    
    public void setTeamMemberships(List<TranslationTeam> teamMemberships) {
		this.teamMemberships = teamMemberships;
	}
    
}
