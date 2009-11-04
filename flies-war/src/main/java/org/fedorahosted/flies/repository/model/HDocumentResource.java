package org.fedorahosted.flies.repository.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.rest.dto.DocumentResource;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

/**
 * Represent a Node in the DOM of a document
 * 
 * @author asgeirf
 *
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public abstract class HDocumentResource implements Serializable{

	private static final long serialVersionUID = 7639343235241588429L;

	private Long id;
	private Integer revision = 1;
	private String resId;
	
	private HDocument document;
	private HDocumentResource parent;
	private boolean obsolete = false;
	
	public HDocumentResource() {
	}
	
	public HDocumentResource(DocumentResource res) {
		this.resId = res.getId();
		this.revision = res.getRevision();
	}

	@Id
	@GeneratedValue	
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
	// TODO make this case sensitive
	@NaturalId
	@Length(max=255)
	@NotEmpty
	public String getResId() {
		return resId;
	}
	
	public void setResId(String resId) {
		this.resId = resId;
	}

	@NotNull
	public Integer getRevision() {
		return revision;
	}
	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	public boolean isObsolete() {
		return obsolete;
	}
	
	public void setObsolete(boolean obsolete) {
		this.obsolete = obsolete;
	}
	
	@ManyToOne
	@JoinColumn(name="document_id",insertable=true, updatable=false, nullable=true)
	@NaturalId
	public HDocument getDocument() {
		return document;
	}
	
	public void setDocument(HDocument document) {
		this.document = document;
	}

	@ManyToOne
	@JoinColumn(name = "parent_id", insertable=true)
	public HDocumentResource getParent() {
		return parent;
	}
	
	public void setParent(HDocumentResource parent) {
		this.parent = parent;
	}

	public abstract DocumentResource toResource(int levels);
}
