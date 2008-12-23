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

@Entity
public class ProjectSeries implements Serializable{
	
    private Long id;
    private Integer version;
    private String name;

    private Project project;

    private ProjectSeries parent;
    private List<ProjectSeries> children;
    
    private List<ProjectTarget> projectTargets;
    
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
    @JoinColumn(name="projectId")
    public Project getProject() {
		return project;
	}
    
    public void setProject(Project project) {
		this.project = project;
	}
    
    @OneToMany(mappedBy="parent")
    public List<ProjectSeries> getChildren() {
		return children;
	}
    
    public void setChildren(List<ProjectSeries> children) {
		this.children = children;
	}
    
    @ManyToOne
    @JoinColumn(name="parentId")
    public ProjectSeries getParent() {
		return parent;
	}
    
    public void setParent(ProjectSeries parent) {
		this.parent = parent;
	}
    
    
    @OneToMany(mappedBy="projectSeries")
    public List<ProjectTarget> getProjectTargets() {
		return projectTargets;
	}
    
    public void setProjectTargets(List<ProjectTarget> projectTargets) {
		this.projectTargets = projectTargets;
	}
    
}
