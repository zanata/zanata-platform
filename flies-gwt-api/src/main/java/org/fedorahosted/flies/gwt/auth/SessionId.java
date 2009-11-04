package org.fedorahosted.flies.gwt.auth;

import java.io.Serializable;

import org.fedorahosted.flies.gwt.model.Identifier;
import org.fedorahosted.flies.gwt.model.PersonId;

public final class SessionId implements Identifier<String>, Serializable {
	private String id;
	
	
	@SuppressWarnings("unused")
	private SessionId() {
	}
	
	public SessionId(String id) {
		if(id == null || id.isEmpty()) {
			throw new IllegalStateException("Invalid Id");
		}
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj instanceof SessionId){
			return ((SessionId)obj).id == id;
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
	
	@Override
	public String getValue() {
		return id;
	}
}
