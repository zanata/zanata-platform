package org.fedorahosted.flies.repository.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.fedorahosted.flies.core.model.ResourceCategory;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
@DiscriminatorValue("doc")
public class Document extends ContainerItem{

	private String contentType;
	private Integer revision = 1;

	private Set<Resource> resources;
	private List<Resource> resourceTree;
	
	private ResourceCategory resourceCategory;
	private List<DocumentTarget> targets = new ArrayList<DocumentTarget>();
	
	@NotNull
	public Integer getRevision() {
		return revision;
	}

	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	@Transient
	public void incrementRevision() {
		revision++;
	}

	@Length(max = 20)
	@NotEmpty
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@OneToMany(mappedBy = "document")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public Set<Resource> getResources() {
		return resources;
	}
	
	public void setResources(Set<Resource> resources) {
		this.resources = resources;
	}
	
	@OneToMany(mappedBy = "template")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<DocumentTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<DocumentTarget> targets) {
		this.targets = targets;
	}

	@ManyToOne
	@JoinColumn(name = "category_id")
	public ResourceCategory getResourceCategory() {
		return resourceCategory;
	}

	public void setResourceCategory(ResourceCategory resourceCategory) {
		this.resourceCategory = resourceCategory;
	}
	
	@OneToMany(mappedBy = "document")
	@Where(clause="parent=NULL")
	@IndexColumn(name="pos")
	@OnDelete(action=OnDeleteAction.CASCADE)
	public List<Resource> getResourceTree() {
		return resourceTree;
	}
	
	public void setResourceTree(List<Resource> resourceTree) {
		this.resourceTree = resourceTree;
	}
}
