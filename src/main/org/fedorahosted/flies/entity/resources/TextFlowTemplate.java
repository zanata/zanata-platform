package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TextFlowTemplate extends AbstractTextFlow{

	private List<TextFlowTarget> targets;
	private DocumentTemplate documentTemplate;
	
	@OneToMany(mappedBy="template")
	public List<TextFlowTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TextFlowTarget> targets) {
		this.targets = targets;
	}
	
	@ManyToOne
	@JoinColumn(name="document_template_id")
	public DocumentTemplate getDocumentTemplate() {
		return documentTemplate;
	}

	public void setDocumentTemplate(DocumentTemplate documentTemplate) {
		this.documentTemplate = documentTemplate;
	}
	
}
