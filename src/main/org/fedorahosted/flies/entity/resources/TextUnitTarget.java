package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.entity.locale.Locale;
import org.hibernate.annotations.NaturalId;

@Entity
public class TextUnitTarget extends AbstractTextUnit{

	private TextUnit template;
	private Locale locale;
	
	public static enum Status{New, FuzzyMatch, ForReview, Approved}
	
	private Status status;

	@ManyToOne
//	@JoinColumns({
//	    @JoinColumn(name="resource_id", insertable=false, updatable=false),
//	    @JoinColumn(name="document_id", insertable=false, updatable=false)
//	})
	@NaturalId
	public TextUnit getTemplate() {
		return template;
	}
	
	public void setTemplate(TextUnit template) {
		this.template = template;
	}

	@ManyToOne
	@NaturalId
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	private DocumentTarget documentTarget;
	
	@ManyToOne
//	@JoinColumns({
//	    @JoinColumn(name="locale_id", insertable=false, updatable=false),
//	    @JoinColumn(name="document_id", insertable=false, updatable=false)
//	})
	public DocumentTarget getDocumentTarget() {
		return documentTarget;
	}
	
	public void setDocumentTarget(DocumentTarget documentTarget) {
		this.documentTarget = documentTarget;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
