package org.fedorahosted.flies.entity.resources;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@MappedSuperclass
public abstract class AbstractTextUnitTemplate extends AbstractTextUnit{

	private boolean obsolete;
	
	private Integer position;

	private String resourceId;

	private Document document;

	
	@NotNull
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

	@NotEmpty
	@Column(name="resource_id")
	//@NaturalId
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@NotNull
	@ManyToOne
	@JoinColumn(name="document_id")
	//@NaturalId
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
}
