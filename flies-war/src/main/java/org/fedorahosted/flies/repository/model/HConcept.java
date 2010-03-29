package org.fedorahosted.flies.repository.model;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.validator.NotNull;

@Entity
public class HConcept extends AbstractFliesEntity{
	
	private Map<LocaleId, HTermEntry> terms;
	
	private HGlossary glossary;
	
	private String term;
	private String comment;
	private String desc;
	
	public void setTerms(Map<LocaleId, HTermEntry> terms) {
		this.terms = terms;
	}
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="concept")
	@MapKey(name="localeId")
	public Map<LocaleId, HTermEntry> getTerms() {
		return terms;
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
	
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	@Column(name = "description")
	public String getDesc() {
		return desc;
	}

	public void setGlossary(HGlossary glossary) {
		this.glossary = glossary;
	}

	@ManyToOne
	@NotNull
	@JoinColumn(name="glossary_id")
	public HGlossary getGlossary() {
		return glossary;
	}
	
	

}
