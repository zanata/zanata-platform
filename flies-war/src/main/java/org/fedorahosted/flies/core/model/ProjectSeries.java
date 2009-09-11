package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.validator.Length;

@Entity
public class ProjectSeries extends AbstractFliesEntity implements Serializable {

	private String name;

	private HIterationProject project;

	private ProjectSeries parent;
	private List<ProjectSeries> children;

	private List<HProjectIteration> projectIterations;

	public static final String DEFAULT = "default";
	
	@Length(max = 20)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ManyToOne
	@JoinColumn(name = "projectId")
	public HIterationProject getProject() {
		return project;
	}

	public void setProject(HIterationProject project) {
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
	public List<HProjectIteration> getProjectIterations() {
		return projectIterations;
	}

	public void setProjectIterations(List<HProjectIteration> projectIterations) {
		this.projectIterations = projectIterations;
	}

	@Transient
	public boolean isDefault(){
		return DEFAULT.equals(getName());
	}
}
