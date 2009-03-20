package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import org.hibernate.validator.Length;

@Entity
public class ProjectSeries extends AbstractFliesEntity implements Serializable {

	private String name;

	private Project project;

	private ProjectSeries parent;
	private List<ProjectSeries> children;

	private List<ProjectTarget> projectTargets;

	@Length(max = 20)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne
	@JoinColumn(name = "projectId")
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@OneToMany(mappedBy = "parent")
	public List<ProjectSeries> getChildren() {
		return children;
	}

	public void setChildren(List<ProjectSeries> children) {
		this.children = children;
	}

	@ManyToOne
	@JoinColumn(name = "parentId")
	public ProjectSeries getParent() {
		return parent;
	}

	public void setParent(ProjectSeries parent) {
		this.parent = parent;
	}

	@OneToMany(mappedBy = "projectSeries")
	public List<ProjectTarget> getProjectTargets() {
		return projectTargets;
	}

	public void setProjectTargets(List<ProjectTarget> projectTargets) {
		this.projectTargets = projectTargets;
	}

}
