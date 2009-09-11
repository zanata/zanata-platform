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
public class HProjectSeries extends AbstractFliesEntity implements Serializable {

	private String name;

	private HIterationProject project;

	private HProjectSeries parent;
	private List<HProjectSeries> children;

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
	public List<HProjectSeries> getChildren() {
		return children;
	}

	public void setChildren(List<HProjectSeries> children) {
		this.children = children;
	}

	@ManyToOne
	@JoinColumn(name = "parentId")
	public HProjectSeries getParent() {
		return parent;
	}

	public void setParent(HProjectSeries parent) {
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
