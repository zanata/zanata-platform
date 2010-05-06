package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;

import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class HProjectIteration extends AbstractSlugEntity implements IProjectContainerProvider, Serializable {

	private String name;
	private String description;

	private HIterationProject project;

	private Boolean active = true;

	private HProjectContainer container;

	private HProjectIteration parent;
	private List<HProjectIteration> children;
	
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
	@JoinColumn(name = "project_container_id", nullable=false)
	@Override
	public HProjectContainer getContainer() {
		return container;
	}
	
	public void setContainer(HProjectContainer container) {
		this.container = container;
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
	public List<HProjectIteration> getChildren() {
		return children;
	}

	public void setChildren(List<HProjectIteration> children) {
		this.children = children;
	}

	@ManyToOne
	@JoinColumn(name = "parentId")
	public HProjectIteration getParent() {
		return parent;
	}

	public void setParent(HProjectIteration parent) {
		this.parent = parent;
	}
	
}
