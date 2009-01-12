package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.fedorahosted.flies.entity.locale.Locale;

@Entity
public class TextUnit extends AbstractTextUnit{

	private Document document;

	private List<TextUnitTarget> targets;
	
	@ManyToOne
	@JoinColumn(name="document_id")
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	@OneToMany(mappedBy="template")
	public List<TextUnitTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<TextUnitTarget> targets) {
		this.targets = targets;
	}
	
}
