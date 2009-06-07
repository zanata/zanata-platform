package org.fedorahosted.flies.repository.model;

import javax.persistence.Entity;

import org.hibernate.validator.NotEmpty;

@Entity
public class Reference extends Resource{

	private static final long serialVersionUID = -9149721887030641326L;

	private String ref;
	
	@NotEmpty
	public String getRef() {
		return ref;
	}
	
	public void setRef(String ref) {
		this.ref = ref;
	}
}
