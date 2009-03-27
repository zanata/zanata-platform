package org.fedorahosted.flies.core.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.fedorahosted.flies.validator.url.Slug;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@Entity
public class Collection extends AbstractFliesEntity implements Serializable {

	private String name;
	private String slug;
	private String shortDescription;
	private String longDescription;

	private List<ProjectTarget> projectTargets;

	@Length(max = 80)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NaturalId
	@Length(min = 2, max = 40)
	@Slug
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	@Length(max = 240)
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

	@ManyToMany
	@JoinTable(name = "Collection_ProjectTarget")
	public List<ProjectTarget> getProjectTargets() {
		return projectTargets;
	}

	public void setProjectTargets(List<ProjectTarget> projectTargets) {
		this.projectTargets = projectTargets;
	}

}
