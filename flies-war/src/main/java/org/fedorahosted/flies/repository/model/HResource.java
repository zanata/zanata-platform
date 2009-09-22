package org.fedorahosted.flies.repository.model;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.Resource;
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
public abstract class HResource implements Serializable{

	private static final long serialVersionUID = 7639343235241588429L;

	private Long id;
	private Long revision = 1l;
	private String resId;
	
	private HDocument document;
	private HResource parent;

	public HResource() {
	}
	
	public HResource(Resource res) {
		this.resId = res.getId();
		this.revision = res.getVersion();
	}

	@Id
	@GeneratedValue	
	public Long getId() {
		return id;
	}
	
	protected void setId(Long id) {
		this.id = id;
	}
	
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
	public Long getRevision() {
		return revision;
	}
	
	public void setRevision(Long revision) {
		this.revision = revision;
	}
	
	@ManyToOne
	@JoinColumn(name="document_id",insertable=false, updatable=false, nullable=false)
	@NaturalId
	public HDocument getDocument() {
		return document;
	}
	
	public void setDocument(HDocument document) {
		this.document = document;
	}

	@ManyToOne
	@JoinColumn(name = "parent_id")
	public HResource getParent() {
		return parent;
	}
	
	public void setParent(HResource parent) {
		this.parent = parent;
	}

	public abstract Resource toResource(Set<LocaleId> includedTargets, int levels);
}
