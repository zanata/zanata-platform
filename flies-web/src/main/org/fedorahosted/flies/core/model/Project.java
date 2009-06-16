package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.fedorahosted.flies.repository.model.Document;
import org.fedorahosted.flies.validators.Slug;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="projecttype",
    discriminatorType=DiscriminatorType.STRING
)
@Indexed
public abstract class Project extends AbstractSlugEntity implements Serializable {

	private String name;
	private String description;
	private String homeContent;

	private List<Document> documents = new ArrayList<Document>();

	private List<Person> maintainers = new ArrayList<Person>();

	@Length(max = 80)
        @Field(index=Index.TOKENIZED)
        public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Length(max = 100)
        @Field(index=Index.TOKENIZED)
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Type(type = "text")
	public String getHomeContent() {
		return homeContent;
	}

	public void setHomeContent(String homeContent) {
		this.homeContent = homeContent;
	}

	@ManyToMany
	@JoinTable(name = "Project_Maintainer", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "personId"))
	public List<Person> getMaintainers() {
		return maintainers;
	}

	public void setMaintainers(List<Person> maintainers) {
		this.maintainers = maintainers;
	}

}
