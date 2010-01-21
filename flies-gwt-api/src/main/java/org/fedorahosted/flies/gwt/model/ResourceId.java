package org.fedorahosted.flies.gwt.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ResourceId implements IsSerializable{
	private Long id;

	private ResourceId() {
	}
	
	public ResourceId(long id) {
		this.id = id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String toString() {
		return String.valueOf(id);
	}
}
