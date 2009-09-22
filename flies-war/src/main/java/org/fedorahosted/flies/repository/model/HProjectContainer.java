package org.fedorahosted.flies.repository.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.annotations.Cascade;

@Entity
public class HProjectContainer extends AbstractFliesEntity{

	private Map<String,HDocument> documents;

	public HProjectContainer() {
	}

	@OneToMany(mappedBy = "project", cascade=CascadeType.ALL)
	@Cascade({ org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
	@MapKey(name="docId")
	public Map<String, HDocument> getDocuments() {
		if(documents == null)
			documents = new HashMap<String,HDocument>();
		return documents;
	}
	

	public void setDocuments(Map<String,HDocument> documents) {
		this.documents = documents;
	}
	
}
