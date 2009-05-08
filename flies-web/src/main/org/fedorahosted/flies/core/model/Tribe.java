package org.fedorahosted.flies.core.model;

import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * A tribe represents people with a common language/locale
 * 
 * @author asgeirf
 *
 */
@Entity
public class Tribe extends AbstractFliesEntity{

	private FliesLocale locale;
	
	private Person chief;
	private Set<Person> tribalLeaders;
	private Set<Person> members;

	
	@OneToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "locale_id")
	public FliesLocale getLocale() {
		return locale;
	}
	
	public void setLocale(FliesLocale locale) {
		this.locale = locale;
	}
	
	@ManyToOne
	@JoinColumn(name = "chiefId")
	public Person getChief() {
		return chief;
	}

	public void setChief(Person chief) {
		this.chief = chief;
	}
	
	@ManyToMany
	@JoinTable(name = "Tribe_Leader", joinColumns = @JoinColumn(name = "tribeId"), inverseJoinColumns = @JoinColumn(name = "personId"))
	public Set<Person> getTribalLeaders() {
		return tribalLeaders;
	}
	
	public void setTribalLeaders(Set<Person> tribalLeaders) {
		this.tribalLeaders = tribalLeaders;
	}
	
	@ManyToMany
	@JoinTable(name = "Tribe_Member", joinColumns = @JoinColumn(name = "tribeId"), inverseJoinColumns = @JoinColumn(name = "personId"))
	public Set<Person> getMembers() {
		return members;
	}
	
	public void setMembers(Set<Person> members) {
		this.members = members;
	}
	
}
