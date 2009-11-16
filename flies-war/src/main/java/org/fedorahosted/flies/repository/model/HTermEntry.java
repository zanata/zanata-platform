package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.fedorahosted.flies.hibernate.type.LocaleIdType;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.validator.NotNull;

@Entity
@TypeDef(name="localeId", typeClass=LocaleIdType.class)
public class HTermEntry extends AbstractFliesEntity{

	private LocaleId localeId;
	private HConcept concept;
	
	private String term;
	private String comment;
	
	public void setLocaleId(LocaleId localeId) {
		this.localeId = localeId;
	}
	
	@NaturalId
	@Type(type="localeId")
	@NotNull
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public void setConcept(HConcept concept) {
		this.concept = concept;
	}
	
	@NaturalId
	@ManyToOne
	@JoinColumn(name="concept_id")
	public HConcept getConcept() {
		return concept;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}

}
