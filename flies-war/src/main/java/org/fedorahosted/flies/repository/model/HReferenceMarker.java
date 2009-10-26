package org.fedorahosted.flies.repository.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.validator.NotNull;

@Entity
@DiscriminatorValue("ref")
public class HReferenceMarker extends HInlineMarker{

	private static final long serialVersionUID = -7985022096809099291L;

	private HDocumentResource resource;
	
	@ManyToOne
	@JoinColumn(name = "resource_id")
	@NotNull
	public HDocumentResource getResource() {
		return resource;
	}
	
	public void setResource(HDocumentResource resource) {
		this.resource = resource;
	}
}
