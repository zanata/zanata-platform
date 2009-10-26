package org.fedorahosted.flies.repository.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.annotations.Where;

@Entity
public class HProjectContainer extends AbstractFliesEntity{

	private Map<String,HDocument> documents;
	private Map<String,HDocument> allDocuments;

	public HProjectContainer() {
	}

	@OneToMany(mappedBy = "project", cascade=CascadeType.ALL)
	@MapKey(name="docId")
	@Where(clause="obsolete=0")
	public Map<String, HDocument> getDocuments() {
		if(documents == null)
			documents = new HashMap<String,HDocument>();
		return documents;
	}
	
	@OneToMany(mappedBy = "project", cascade=CascadeType.ALL)
	@MapKey(name="docId")
	// even obsolete documents
	public Map<String, HDocument> getAllDocuments() {
		if(allDocuments == null)
			allDocuments = new HashMap<String,HDocument>();
		return allDocuments;
	}
	
	public void setAllDocuments(Map<String, HDocument> allDocuments) {
		this.allDocuments = allDocuments;
	}
	
	public void setDocuments(Map<String, HDocument> documents) {
		this.documents = documents;
	}

}
