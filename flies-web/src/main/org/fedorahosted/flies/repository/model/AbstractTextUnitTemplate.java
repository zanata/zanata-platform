package org.fedorahosted.flies.repository.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@MappedSuperclass
public abstract class AbstractTextUnitTemplate extends AbstractTextUnit {

	private boolean obsolete;

	private Integer pos = -1;

	private String resourceId;

	private Document document;

	@NotNull
	public boolean isObsolete() {
		return obsolete;
	}

	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}

	@NotNull
	public Integer getPos() {
		return pos;
	}

	public void setPos(Integer pos) {
		this.pos = pos;
	}

	@Column(name = "resource_id")
	@Length(max=255)
	//@NaturalId
	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@ManyToOne
	@JoinColumn(name = "document_id")
	//@NaturalId
	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
