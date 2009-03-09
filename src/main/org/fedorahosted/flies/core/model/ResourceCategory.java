package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.fedorahosted.flies.resources.model.Document;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class ResourceCategory extends AbstractFliesEntity implements Serializable{
	
    private String name;

    private Project project;
    
    private List<Document> documents;
    
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
    
	@OneToMany(mappedBy="resourceCategory")
    public List<Document> getDocuments() {
		return documents;
	}
    
    public void setDocuments(List<Document> documents) {
		this.documents = documents;
	}
    
}
