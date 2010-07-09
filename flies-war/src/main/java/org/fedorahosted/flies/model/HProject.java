package org.fedorahosted.flies.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import org.hibernate.annotations.Type;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotEmpty;
import org.jboss.seam.annotations.security.Restrict;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="projecttype",
    discriminatorType=DiscriminatorType.STRING
)
@Restrict
public abstract class HProject extends AbstractSlugEntity implements Serializable {

	private String name;
	private String description;
	private String homeContent;

	private Set<HPerson> maintainers;

	@Length(max = 80)
	@NotEmpty
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
	@JoinTable(name = "HProject_Maintainer", joinColumns = @JoinColumn(name = "projectId"), inverseJoinColumns = @JoinColumn(name = "personId"))
	public Set<HPerson> getMaintainers() {
		if(maintainers == null)
			maintainers = new HashSet<HPerson>();
		return maintainers;
	}

	public void setMaintainers(Set<HPerson> maintainers) {
		this.maintainers = maintainers;
	}

	@Override
	public String toString() {
		return super.toString()+"[name="+name+"]";
	}
	
}
