package org.fedorahosted.flies.entity.resources;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TextUnit extends AbstractTextUnit{

	private DocumentTarget documentTarget;
	private TextUnitTarget template;
	
	@ManyToOne
	@JoinColumn(name="template_id")
	public TextUnitTarget getTemplate() {
		return template;
	}
	
	public void setTemplate(TextUnitTarget template) {
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
