package org.fedorahosted.flies.core.model;

import javax.persistence.MappedSuperclass;

import org.fedorahosted.flies.validator.url.Slug;
import org.hibernate.annotations.NaturalId;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;

@MappedSuperclass
public class AbstractSlugEntity extends AbstractFliesEntity{
	private String slug;

	@NaturalId
	@Length(min = 2, max = 40)
	@Slug
	@NotNull
	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

}
