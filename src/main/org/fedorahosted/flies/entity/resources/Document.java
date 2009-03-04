package org.fedorahosted.flies.entity.resources;

import java.io.Serializable;
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

import org.fedorahosted.flies.entity.FliesLocale;
import org.fedorahosted.flies.entity.Project;
import org.fedorahosted.flies.entity.ProjectTarget;
import org.fedorahosted.flies.entity.ResourceCategory;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;

@Entity
public class Document implements Serializable{

	private Long id;
    private Integer version;
	
    private String name;
    
    private String contentType;
    
	private List<DocumentTarget> targets;
	
	private List<TextUnit> entries;

	private Integer revision = 1;
	
	private Project project;
	private ProjectTarget projectTarget;

	private List<TextUnitTarget> targetEntries;
	
	private ResourceCategory resourceCategory;

	@NotNull
	public Integer getRevision() {
		return revision;
	}
	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	@Transient
	public void incrementRevision(){
		revision++;
	}
	
	@Id @GeneratedValue
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
    
    @Length(max=20)
    @NotEmpty
    public String getContentType() {
		return contentType;
	}
    
    public void setContentType(String contentType) {
		this.contentType = contentType;
	}
    
    @ManyToOne
    @JoinColumn(name="projectId")
    public Project getProject() {
		return project;
	}
    
    public void setProject(Project project) {
		this.project = project;
	}
    
    @ManyToOne
    @JoinColumn(name="projectTargetId")
    public ProjectTarget getProjectTarget() {
		return projectTarget;
	}
    
    public void setProjectTarget(ProjectTarget projectTarget) {
		this.projectTarget = projectTarget;
	}
    
	@OneToMany(mappedBy="template")
	public List<DocumentTarget> getTargets() {
		return targets;
	}
	
	public void setTargets(List<DocumentTarget> targets) {
		this.targets = targets;
	}
	
	@OneToMany(mappedBy="document")
	@OrderBy("position")
	public List<TextUnit> getEntries() {
		return entries;
	}
	
	public void setEntries(List<TextUnit> entries) {
		this.entries = entries;
	}
	
	@OneToMany(mappedBy="document")
	public List<TextUnitTarget> getTargetEntries() {
		return targetEntries;
	}
	
	@OneToMany(mappedBy="document")
	public void setTargetEntries(List<TextUnitTarget> targetEntries) {
		this.targetEntries = targetEntries;
	}

    @ManyToOne
    @JoinColumn(name="category_id")
	public ResourceCategory getResourceCategory() {
		return resourceCategory;
	}
	
	public void setResourceCategory(ResourceCategory resourceCategory) {
		this.resourceCategory = resourceCategory;
	}
	
}
