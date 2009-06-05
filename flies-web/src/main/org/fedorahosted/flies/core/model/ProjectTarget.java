package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.fedorahosted.flies.repository.model.Document;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class ProjectTarget extends AbstractSlugEntity implements Serializable {

	private String name;

	private String description;

	private ProjectSeries projectSeries;

	private Project project;

	private Boolean active = true;
	
	private ProjectTarget parent;
	private List<ProjectTarget> children;
	private List<Document> documents;

	private String localDirectory;
	
	@Length(max = 20)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public String getLocalDirectory() {
		return localDirectory;
	}
	
	public void setLocalDirectory(String localDirectory) {
		this.localDirectory = localDirectory;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	@NotNull
	public Boolean getActive() {
		return active;
	}
	
	@ManyToOne
	@JoinColumn(name = "projectSeriesId")
	@NotNull
	public ProjectSeries getProjectSeries() {
		return projectSeries;
	}

	public void setProjectSeries(ProjectSeries projectSeries) {
		this.projectSeries = projectSeries;
	}

	@ManyToOne
	@NotNull
	@NaturalId
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@OneToMany(mappedBy = "parent")
	public List<ProjectTarget> getChildren() {
		return children;
	}

	public void setChildren(List<ProjectTarget> children) {
		this.children = children;
	}

	@ManyToOne
	@JoinColumn(name = "parentId")
	public ProjectTarget getParent() {
		return parent;
	}

	public void setParent(ProjectTarget parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy = "projectTarget")
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;

	}
}
