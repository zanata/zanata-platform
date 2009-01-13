package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.AssociationOverride;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.entity.locale.Locale;

@Entity
public class TextUnitTarget extends AbstractTextUnit{
	private TextUnitTargetId id;
	
	private DocumentTarget documentTarget;

	public static enum Status{New, FuzzyMatch, ForReview, Approved}
	
	private Status status;
	
	@Id
	public TextUnitTargetId getId() {
		return id;
	}
	
	public void setId(TextUnitTargetId id) {
		this.id = id;
	}
	
	@ManyToOne
	// TODO: link foreign keys between documenTarget and textUnit
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
