package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.fedorahosted.flies.rest.dto.DocumentView;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.hibernate.annotations.IndexColumn;

@Entity
public class HProjectContainer extends AbstractFliesEntity{

	private List<HDocument> documents;

	public HProjectContainer() {
	}

	@IndexColumn(name="pos",base=0,nullable=false)
	@JoinColumn(name="project_id",nullable=false)
	@OneToMany(cascade=CascadeType.ALL)
	//@OnDelete(action=OnDeleteAction.CASCADE)
	public List<HDocument> getDocuments() {
		if(documents == null)
			documents = new ArrayList<HDocument>();
		return documents;
	}

	public void setDocuments(List<HDocument> documents) {
		this.documents = documents;
	}
	
}
