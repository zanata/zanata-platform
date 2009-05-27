package org.fedorahosted.flies.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Transient;
import javax.persistence.Version;

import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.core.model.ResourceCategory;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
public class Document implements Serializable {

	private Long id;
	private Integer version;

	private String name;

	private String contentType;

	private List<DocumentTarget> targets = new ArrayList<DocumentTarget>();

	private List<TextUnit> entries = new ArrayList<TextUnit>();

	private Integer revision = 1;

	private Project project;
	private ProjectTarget projectTarget;

	private List<TextUnitTarget> targetEntries = new ArrayList<TextUnitTarget>();

	private ResourceCategory resourceCategory;

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

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}

	private void setId(Long id) {
		this.id = id;
	}

	@Version
	public Integer getVersion() {
		return version;
	}

	private void setVersion(Integer version) {
		this.version = version;
	}

	@NotEmpty
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Length(max = 20)
	@NotEmpty
	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "projectId")
	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "projectTargetId")
	public ProjectTarget getProjectTarget() {
		return projectTarget;
	}

	public void setProjectTarget(ProjectTarget projectTarget) {
		this.projectTarget = projectTarget;
	}

	@OneToMany(mappedBy = "template")
	public List<DocumentTarget> getTargets() {
		return targets;
	}

	public void setTargets(List<DocumentTarget> targets) {
		this.targets = targets;
	}

	@OneToMany(mappedBy = "document")
	@OrderBy("pos")
	public List<TextUnit> getEntries() {
		return entries;
	}

	public void setEntries(List<TextUnit> entries) {
		this.entries = entries;
	}

	@OneToMany(mappedBy = "document")
	public List<TextUnitTarget> getTargetEntries() {
		return targetEntries;
	}

	@OneToMany(mappedBy = "document")
	public void setTargetEntries(List<TextUnitTarget> targetEntries) {
		this.targetEntries = targetEntries;
	}

	@NotNull
	@ManyToOne
	@JoinColumn(name = "category_id")
	public ResourceCategory getResourceCategory() {
		return resourceCategory;
	}

	public void setResourceCategory(ResourceCategory resourceCategory) {
		this.resourceCategory = resourceCategory;
	}

}
