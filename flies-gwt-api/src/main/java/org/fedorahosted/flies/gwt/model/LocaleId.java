package org.fedorahosted.flies.gwt.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LocaleId implements Identifier<String>, IsSerializable{

	private String id;

	@SuppressWarnings("unused")
	private LocaleId() {
	}
	
	public LocaleId(String id) {
		this.id = id;
	}
	
	@Override
	public String toString() {
		return String.valueOf(id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public String getValue(){
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof LocaleId) {
			return ((LocaleId) obj).id == id;
		}
		return false;
	}

}
