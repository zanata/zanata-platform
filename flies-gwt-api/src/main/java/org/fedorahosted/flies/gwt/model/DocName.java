package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocName implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;
	private DocumentId id;
	private String name;
	private String path;
	
	@SuppressWarnings("unused")
	private DocName() {
	}

	public DocName(DocumentId id, String name, String path) {
		this.id = id;
		this.name = name;
		this.path = path;
	}

	public DocumentId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
	
	@Override
	public String toString() {
		return "DocName(name="+name+",path="+path+",id="+id+")";
	}
	
}
