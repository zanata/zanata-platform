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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.fedorahosted.flies.entity.locale.Locale;

@Entity
@Table(	uniqueConstraints = {@UniqueConstraint(columnNames={"template_id", "locale_id"})})
public class TextUnitTarget extends AbstractTextUnit{

	private TextUnit template;
	private Locale locale;
	
	public static enum Status{New, FuzzyMatch, ForReview, Approved}
	
	private Status status;

	@ManyToOne
	@JoinColumn(name="template_id")
	//@NaturalId
	public TextUnit getTemplate() {
		return template;
	}
	
	public void setTemplate(TextUnit template) {
		this.template = template;
	}

	@ManyToOne
	@JoinColumn(name="locale_id")
	//@NaturalId
	public Locale getLocale() {
		return locale;
	}
	
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	private DocumentTarget documentTarget;
	
	@ManyToOne
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
