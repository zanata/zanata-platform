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
public class CollectionTarget extends AbstractFliesEntity implements Serializable{
	
    private String name;

    private String description;
    
    private CollectionSeries collectionSeries;
    
    private Collection collection;

    private CollectionTarget parent;
    private List<CollectionTarget> children;
    
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
    @JoinColumn(name="collectionSeriesId")
    @NotNull
    public CollectionSeries getCollectionSeries() {
		return collectionSeries;
	}
    
    public void setCollectionSeries(CollectionSeries collectionSeries) {
		this.collectionSeries = collectionSeries;
	}
    
    @ManyToOne
    @JoinColumn(name="collectionId")
    public Collection getCollection() {
		return collection;
	}
    
    public void setCollection(Collection collection) {
		this.collection = collection;
	}
    
    @OneToMany(mappedBy="parent")
    public List<CollectionTarget> getChildren() {
		return children;
	}
    
    public void setChildren(List<CollectionTarget> children) {
		this.children = children;
	}
    
    @ManyToOne
    @JoinColumn(name="parentId")
    public CollectionTarget getParent() {
		return parent;
	}

    public void setParent(CollectionTarget parent) {
		this.parent = parent;
	}
    
}
