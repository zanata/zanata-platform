package org.fedorahosted.flies.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.fedorahosted.flies.entity.locale.Locale;
import org.hibernate.validator.Length;

@Entity
public class TranslationTeam implements Serializable{

    private Long id;
    private Integer version;
    private String name;
    private String shortDescription;

    private Person coordinator;
    
    private List<Person> members;
    private List<Locale> locales;
    
    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    @Length(max = 20)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Length(max = 100)
    public String getShortDescription() {
		return shortDescription;
	}
    
    public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
    
    @ManyToOne
    @JoinColumn(name="coordinatorId")
    public Person getCoordinator() {
		return coordinator;
	}
    
    public void setCoordinator(Person coordinator) {
		this.coordinator = coordinator;
	}
    
    @ManyToMany
    @JoinTable(
            name="TranslationTeam_Member",
            joinColumns=@JoinColumn(name="translationTeamId"),
            inverseJoinColumns=@JoinColumn(name="personId")
        )
    public List<Person> getMembers() {
		return members;
	}

    public void setMembers(List<Person> members) {
		this.members = members;
	}
    
    
    @ManyToMany
    @JoinTable(
            name="TranslationTeam_Locale",
            joinColumns=@JoinColumn(name="translationTeamId"),
            inverseJoinColumns=@JoinColumn(name="localeId")
        )
    public List<Locale> getLocales() {
		return locales;
	}
    
    public void setLocales(List<Locale> locales) {
		this.locales = locales;
	}
}
