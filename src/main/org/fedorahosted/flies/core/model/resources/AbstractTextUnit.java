package org.fedorahosted.flies.core.model.resources;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@MappedSuperclass
public abstract class AbstractTextUnit implements Serializable{
	
	private Long id;
	
    private Integer version;
	private String content;

	private Integer documentRevision;
	
	public AbstractTextUnit() {
	}
	
	public AbstractTextUnit(Integer documentRevision) {
		this.documentRevision = documentRevision;
	}

	public AbstractTextUnit(AbstractTextUnit other) {
		this.documentRevision = other.documentRevision;
		this.content = other.content;
	}

	
	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	
	private void setId(Long id) {
		this.id = id;
	}
	
	@Column(name="document_revision")
	@NotNull
	public Integer getDocumentRevision() {
		return documentRevision;
	}
	
	public void setDocumentRevision(Integer documentRevision) {
		this.documentRevision = documentRevision;
	}

    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }
	
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@NotEmpty
	@Type(type="text")
	public String getContent() {
		return content;
	}

}
