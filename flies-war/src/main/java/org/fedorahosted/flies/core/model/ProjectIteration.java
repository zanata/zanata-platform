package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class ProjectIteration extends AbstractSlugEntity implements IProjectContainerProvider, Serializable {

	private String name;
	private String description;

	private ProjectSeries projectSeries;
	private HIterationProject project;

	private Boolean active = true;

	private HProjectContainer container;

	private ProjectIteration parent;
	private List<ProjectIteration> children;
	
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

	@NotNull
	@ManyToOne
	@JoinColumn(name = "project_container_id")
	@Override
	public HProjectContainer getContainer() {
		return container;
	}
	
	public void setContainer(HProjectContainer container) {
		this.container = container;
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
	public HIterationProject getProject() {
		return project;
	}

	public void setProject(HIterationProject project) {
		this.project = project;
	}

	@OneToMany(mappedBy = "parent")
	public List<ProjectIteration> getChildren() {
		return children;
	}

	public void setChildren(List<ProjectIteration> children) {
		this.children = children;
	}

	@ManyToOne
	@JoinColumn(name = "parentId")
	public ProjectIteration getParent() {
		return parent;
	}

	public void setParent(ProjectIteration parent) {
		this.parent = parent;
	}
	
}
