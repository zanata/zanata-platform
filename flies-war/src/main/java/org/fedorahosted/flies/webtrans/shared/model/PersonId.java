package org.fedorahosted.flies.webtrans.shared.model;

import java.io.Serializable;

//@ExposeEntity 
public final class PersonId implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;
	
	// for ExposeEntity
	public PersonId() {
	}
	
	public PersonId(String id) {
		if(id == null || id.isEmpty()) {
			throw new IllegalStateException("Invalid Id");
		}
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj instanceof PersonId){
			return ((PersonId)obj).id.equals(id);
		}
		return super.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
}
