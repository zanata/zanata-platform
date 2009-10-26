package org.fedorahosted.flies.repository.model;

import java.util.Set;

import javax.persistence.Entity;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.rest.dto.Reference;
import org.hibernate.validator.NotEmpty;

@Entity
public class HReference extends HDocumentResource{

	private static final long serialVersionUID = -9149721887030641326L;

	private String ref;
	
	public HReference() {
	}
	
	public HReference(Reference reference) {
		super(reference);
		// TODO
		this.ref = reference.getRelationshipId();
	}

	@NotEmpty
	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}
	
	@Override
	public Reference toResource(int levels) {
		Reference reference = new Reference(this.getResId());
		reference.setRelationshipId(this.getRef());
		return reference;
	}
}
