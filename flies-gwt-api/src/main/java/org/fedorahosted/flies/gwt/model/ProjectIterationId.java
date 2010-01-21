package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProjectIterationId implements Serializable, IsSerializable{

	private static final long serialVersionUID = 1L;

	private long id;
	
	@SuppressWarnings("unused")
	private ProjectIterationId() {
	}
	
	public ProjectIterationId(long id) {
		this.id = id;
	}
	
	
	public long getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return String.valueOf(id);
	}

	@Override
	public int hashCode() {
		return (int) id;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof ProjectIterationId) {
			return ((ProjectIterationId) obj).id == id;
		}
		return false;
	}
}
