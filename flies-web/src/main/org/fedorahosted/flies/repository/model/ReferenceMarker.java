package org.fedorahosted.flies.repository.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
@DiscriminatorValue("ref")
public class ReferenceMarker extends InlineMarker{

	private static final long serialVersionUID = -7985022096809099291L;

	private Resource resource;
	
	@ManyToOne
	@JoinColumn(name = "resource_id")
	@NotNull
	public Resource getResource() {
		return resource;
	}
	
	public void setResource(Resource resource) {
		this.resource = resource;
	}
}
