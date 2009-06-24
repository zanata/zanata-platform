package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.core.model.AbstractFliesEntity;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

@Entity
public class ProjectContainer extends AbstractFliesEntity{

	private List<Document> documents = new ArrayList<Document>();
	
	@IndexColumn(name="pos")
	@OneToMany(mappedBy="container",cascade=CascadeType.ALL)
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

}
