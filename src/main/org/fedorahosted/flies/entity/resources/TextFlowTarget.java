package org.fedorahosted.flies.entity.resources;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TextFlowTarget extends AbstractTextFlow{

	DocumentTarget documentTarget;
	TextFlowTemplate template;
	
	@ManyToOne
	@JoinColumn(name="template_id")
	public TextFlowTemplate getTemplate() {
		return template;
	}
	
	public void setTemplate(TextFlowTemplate template) {
		this.template = template;
	}
	
	@ManyToOne
	@JoinColumn(name="document_id")
	public DocumentTarget getDocumentTarget() {
		return documentTarget;
	}
	
	public void setDocumentTarget(DocumentTarget documentTarget) {
		this.documentTarget = documentTarget;
	}
	
}
