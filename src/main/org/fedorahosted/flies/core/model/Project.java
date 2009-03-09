package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.SessionContext;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.fedorahosted.flies.core.model.resources.Document;
import org.fedorahosted.flies.validator.url.Slug;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;
import org.jboss.seam.contexts.Contexts;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
public class Project extends AbstractFliesEntity implements Serializable {

    private String name;
    
    private String slug;
    
    private String shortDescription;
    
    private String longDescription;
    
    private List<ProjectSeries> projectSeries;
    private List<ProjectTarget> projectTargets;

    private List<Document> documents;

    private List<Person> maintainers;
    
    @Length(max = 80)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //@NaturalId
    @Length(min = 2, max = 40)
    @NotNull
    @Slug
    public String getSlug() {
		return slug;
	}
    
    public void setSlug(String slug) {
		this.slug = slug;
	}
    
    @Length(max = 100)
    public String getShortDescription() {
		return shortDescription;
	}
    
    public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

    
    public String getLongDescription() {
		return longDescription;
	}
    
    public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}
    
    @OneToMany(mappedBy="project")
    public List<ProjectSeries> getProjectSeries() {
		return projectSeries;
	}
    
    public void setProjectSeries(List<ProjectSeries> projectSeries) {
		this.projectSeries = projectSeries;
	}
    
    @OneToMany(mappedBy="project")
    public List<Document> getDocuments() {
		return documents;
	}
    
    public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
    
    @OneToMany(mappedBy="project")
    public List<ProjectTarget> getProjectTargets() {
		return projectTargets;
	}
    
    public void setProjectTargets(List<ProjectTarget> projectTargets) {
		this.projectTargets = projectTargets;
	}
    
    @ManyToMany
    @JoinTable(
            name="Project_Maintainer",
            joinColumns=@JoinColumn(name="projectId"),
            inverseJoinColumns=@JoinColumn(name="personId")
        )
    public List<Person> getMaintainers() {
		return maintainers;
	}
    
    public void setMaintainers(List<Person> maintainers) {
		this.maintainers = maintainers;
	}
    
}
