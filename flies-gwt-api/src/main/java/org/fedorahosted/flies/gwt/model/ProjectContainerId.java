package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ProjectContainerId implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id;
	
	@SuppressWarnings("unused")
	private ProjectContainerId() {
	}
	
	public ProjectContainerId(long id) {
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
		if(obj instanceof ProjectContainerId) {
			return ((ProjectContainerId) obj).id == id;
		}
		return false;
	}
}
