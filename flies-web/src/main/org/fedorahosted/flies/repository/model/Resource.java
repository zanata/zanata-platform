package org.fedorahosted.flies.repository.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

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
public abstract class Resource implements Serializable{

	private static final long serialVersionUID = 7639343235241588429L;

	private Long id;
	private String resId;
	private String name;
	
	private Document document;
	
	private Resource parent;

	@Id
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

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@ManyToOne
	@JoinColumn(name = "document_id")
	@NaturalId
	@NotNull
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}

	@ManyToOne
	@JoinColumn(name = "parent_id")
	public Resource getParent() {
		return parent;
	}
	
	public void setParent(Resource parent) {
		this.parent = parent;
	}
}
