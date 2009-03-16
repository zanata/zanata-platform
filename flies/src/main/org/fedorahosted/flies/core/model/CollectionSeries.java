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

import org.hibernate.validator.Length;

@Entity
public class CollectionSeries extends AbstractFliesEntity implements Serializable{
	
    private String name;

    private Collection collection;

    private CollectionSeries parent;
    private List<CollectionSeries> children;
    
    private List<CollectionTarget> collectionTargets;

    @Length(max = 20)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    public List<CollectionSeries> getChildren() {
		return children;
	}
    
    public void setChildren(List<CollectionSeries> children) {
		this.children = children;
	}
    
    @ManyToOne
    @JoinColumn(name="parentId")
    public CollectionSeries getParent() {
		return parent;
	}
    
    public void setParent(CollectionSeries parent) {
		this.parent = parent;
	}
    
    
    @OneToMany(mappedBy="collectionSeries")
    public List<CollectionTarget> getCollectionTargets() {
		return collectionTargets;
	}
    
    public void setCollectionTargets(List<CollectionTarget> collectionTargets) {
		this.collectionTargets = collectionTargets;
	}
    
}
