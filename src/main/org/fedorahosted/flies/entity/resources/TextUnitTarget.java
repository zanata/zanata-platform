package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TextUnitTarget extends AbstractTextUnit{

	private TextUnit template;
	private DocumentTarget documentTarget;

	public static enum Status{New, FuzzyMatch, ForReview, Approved}
	
	private Status status;
	
	@ManyToOne
	@JoinColumn(name="documentTarget_id")
	public DocumentTarget getDocumentTarget() {
		return documentTarget;
	}
	
	public void setDocumentTarget(DocumentTarget documentTarget) {
		this.documentTarget = documentTarget;
	}
	
	@ManyToOne
	@JoinColumn(name="template_id")
	public TextUnit getTemplate() {
		return template;
	}
	
	public void setTemplate(TextUnit template) {
		this.template = template;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
