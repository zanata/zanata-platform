package org.fedorahosted.flies.repository.model.project;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.repository.model.AbstractEntity;
import org.fedorahosted.flies.repository.model.document.HDocument;
import org.fedorahosted.flies.rest.dto.DocumentRef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.validator.NotEmpty;

@Entity
public class HProject extends AbstractEntity{

	private String projectId;
	private String name;
	private String summary;
	
	private List<HDocument> documents;

	public HProject() {
	}

	public HProject(String projectId, String name, String summary) {
		this.projectId = projectId;
		this.name = name;
		this.summary = summary;
	}

	public HProject(org.fedorahosted.flies.rest.dto.Project project) {
		this.projectId = project.getId();
		this.name = project.getName();
		this.summary = project.getSummary();
		for(DocumentRef d : project.getDocuments() ){
			HDocument doc = new HDocument(d);
			this.getDocuments().add(doc);
		}
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
	
	@NotEmpty
	public String getProjectId() {
		return projectId;
	}
	
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public void setSummary(String summary) {
		this.summary = summary;
	}
	
}
