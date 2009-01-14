package org.fedorahosted.flies.entity.resources;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.fedorahosted.flies.entity.locale.Locale;

@Entity
public class TextUnit extends AbstractTextUnit{

	private List<TextUnitTarget> targets;
	
	private boolean obsolete;
	
	private Integer position;

	private String resourceId;

	private Document document;
	
	@Column(name="resource_id")
	//@NaturalId
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@ManyToOne
	@JoinColumn(name="document_id")
	//@NaturalId
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

	public boolean isObsolete() {
		return obsolete;
	}
	
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	public Integer getPosition() {
		return position;
	}
	
	public void setPosition(Integer position) {
		this.position = position;
	}
	
}
