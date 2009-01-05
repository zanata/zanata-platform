package org.fedorahosted.flies.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class ResourceCategory implements Serializable{
	
    private Long id;
    private Integer version;
    private String name;

    private ProjectSeries projectSeries;
    
    private Project project;

    private ResourceCategory parent;
    private List<ResourceCategory> children;
    
    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    private void setVersion(Integer version) {
        this.version = version;
    }

    @Length(max = 20)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ManyToOne
    @JoinColumn(name="projectSeriesId")
    @NotNull
    public ProjectSeries getProjectSeries() {
		return projectSeries;
	}
    
    public void setProjectSeries(ProjectSeries projectSeries) {
		this.projectSeries = projectSeries;
	}
    
    @ManyToOne
    @JoinColumn(name="projectId")
    public Project getProject() {
		return project;
	}
    
    public void setProject(Project project) {
		this.project = project;
	}
    
    @OneToMany(mappedBy="parent")
    public List<ResourceCategory> getChildren() {
		return children;
	}
    
    public void setChildren(List<ResourceCategory> children) {
		this.children = children;
	}
    
    @ManyToOne
    @JoinColumn(name="parentId")
    public ResourceCategory getParent() {
		return parent;
	}
    
    public void setParent(ResourceCategory parent) {
		this.parent = parent;
	}
    
}
