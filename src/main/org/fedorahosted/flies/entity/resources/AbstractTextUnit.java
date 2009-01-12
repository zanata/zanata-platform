package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;

@MappedSuperclass
public abstract class AbstractTextUnit implements Serializable{
	
	private Long id;
    private Integer version;
	private String content;

	private Integer documentRevision;
	
	public Integer getDocumentRevision() {
		return documentRevision;
	}
	
	public void setDocumentRevision(Integer documentRevision) {
		this.documentRevision = documentRevision;
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	
	private void setId(Long id) {
		this.id = id;
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
	
	@Type(type="text")
	public String getContent() {
		return content;
	}

}
