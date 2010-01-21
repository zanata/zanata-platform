package org.fedorahosted.flies.gwt.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TransUnitId implements IsSerializable, Identifier<Long>{
	
	private static final long serialVersionUID = 6291339842619640513L;

	private long id;
	
	@SuppressWarnings("unused")
	private TransUnitId() {
	}
	
	public TransUnitId(long id) {
		this.id = id;
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
	public Long getValue(){
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if(obj instanceof TransUnitId) {
			return ((TransUnitId) obj).id == id;
		}
		return false;
	}
	
}
