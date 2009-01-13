package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Embeddable
public class TextUnitId implements Serializable{

	private String resourceId;
	private Document document;
	
	public String getResourceId() {
		return resourceId;
	}
	
	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	@ManyToOne
	@JoinColumn(name="document_id")
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		
		final TextUnitId other = (TextUnitId) obj;

		if(resourceId == null && other.resourceId != null) return false;
		else if(!resourceId.equals(other.resourceId)) return false;
		
		if(document == null && other.document != null) return false;
		else if(!document.equals(other.document)) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
	    final int prime = 31; 
	    int result = 1; 
	    result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode()); 
	    result = prime * result + ((document == null) ? 0 : document.hashCode()); 
	    return result;  	
	}
}
