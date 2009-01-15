package org.fedorahosted.flies.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.fedorahosted.flies.entity.resources.Document;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class ProjectTarget implements Serializable{
	
    private Long id;
    private Integer version;
    private String name;

    private String description;
    
    private ProjectSeries projectSeries;
    
    private Project project;

    private ProjectTarget parent;
    private List<ProjectTarget> children;
    private List<Document> documents;
    
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

    public String getDescription() {
		return description;
	}
    
    public void setDescription(String description) {
		this.description = description;
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
    public List<ProjectTarget> getChildren() {
		return children;
	}
    
    public void setChildren(List<ProjectTarget> children) {
		this.children = children;
	}
    
    @ManyToOne
    @JoinColumn(name="parentId")
    public ProjectTarget getParent() {
		return parent;
	}

    public void setParent(ProjectTarget parent) {
		this.parent = parent;
	}
    
    @OneToMany(mappedBy="projectTarget")
    public List<Document> getDocuments() {
		return documents;
	}
    
    public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
}
