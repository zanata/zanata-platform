package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class TextUnitTarget extends AbstractTextUnit{

	private List<TextUnit> targets;
	private Document documentTemplate;
	
	@OneToMany(mappedBy="template")
	public List<TextUnit> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TextUnit> targets) {
		this.targets = targets;
	}
	
	@ManyToOne
	@JoinColumn(name="document_template_id")
	public Document getDocumentTemplate() {
		return documentTemplate;
	}

	public void setDocumentTemplate(Document documentTemplate) {
		this.documentTemplate = documentTemplate;
	}
	
}
