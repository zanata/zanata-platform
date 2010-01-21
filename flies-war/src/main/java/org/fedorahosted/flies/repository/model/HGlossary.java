package org.fedorahosted.flies.repository.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;

@Entity
public class HGlossary extends AbstractFliesEntity{
	
	private Set<HConcept> concepts;

	public void setConcepts(Set<HConcept> concepts) {
		this.concepts = concepts;
	}

	@OneToMany(mappedBy = "glossary")
	public Set<HConcept> getConcepts() {
		return concepts;
	}

}
