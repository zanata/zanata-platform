package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.validator.url.Slug;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class Project extends AbstractFliesEntity implements Serializable {

	private String name;

	private String slug;

	private String description;

	private String homeContent;

	private List<ProjectSeries> projectSeries;
	private List<ProjectTarget> projectTargets;

	private List<Document> documents;

	private List<Person> maintainers;

	@Length(max = 80)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NaturalId
	@Length(min = 2, max = 40)
	@Slug
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	@Length(max = 100)
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Type(type = "text")
	public String getHomeContent() {
		return homeContent;
	}

	public void setHomeContent(String homeContent) {
		this.homeContent = homeContent;
	}

	@OneToMany(mappedBy = "project")
	public List<ProjectSeries> getProjectSeries() {
		return projectSeries;
	}

	public void setProjectSeries(List<ProjectSeries> projectSeries) {
		this.projectSeries = projectSeries;
	}

	@OneToMany(mappedBy = "project")
	public List<Document> getDocuments() {
		return documents;
	}

	public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}

	@OneToMany(mappedBy = "project")
	public List<ProjectTarget> getProjectTargets() {
		return projectTargets;
	}

	public void setProjectTargets(List<ProjectTarget> projectTargets) {
		this.projectTargets = projectTargets;
	}

	@ManyToMany
	@JoinTable(name = "Project_Maintainer", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "personId"))
	public List<Person> getMaintainers() {
		return maintainers;
	}

	public void setMaintainers(List<Person> maintainers) {
		this.maintainers = maintainers;
	}

}
