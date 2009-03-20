package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import org.hibernate.validator.Length;

@Entity
public class TranslationTeam extends AbstractFliesEntity implements
		Serializable {

	private String name;
	private String shortDescription;

	private Person coordinator;

	private List<Person> members;
	private List<FliesLocale> locales;

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
	@JoinColumn(name = "coordinatorId")
	public Person getCoordinator() {
		return coordinator;
	}

	public void setCoordinator(Person coordinator) {
		this.coordinator = coordinator;
	}

	@ManyToMany
	@JoinTable(name = "TranslationTeam_Member", joinColumns = @JoinColumn(name = "translationTeamId"), inverseJoinColumns = @JoinColumn(name = "personId"))
	public List<Person> getMembers() {
		return members;
	}

	public void setMembers(List<Person> members) {
		this.members = members;
	}

	@ManyToMany
	@JoinTable(name = "TranslationTeam_Locale", joinColumns = @JoinColumn(name = "translationTeamId"), inverseJoinColumns = @JoinColumn(name = "localeId"))
	public List<FliesLocale> getLocales() {
		return locales;
	}

	public void setLocales(List<FliesLocale> locales) {
		this.locales = locales;
	}
}
