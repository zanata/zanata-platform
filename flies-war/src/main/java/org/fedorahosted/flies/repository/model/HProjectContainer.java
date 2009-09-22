package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.hibernate.annotations.IndexColumn;

@Entity
public class HProjectContainer extends AbstractFliesEntity{

	private Map<String,HDocument> documents;

	public HProjectContainer() {
	}

	@OneToMany(mappedBy = "project", cascade=CascadeType.ALL)
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
