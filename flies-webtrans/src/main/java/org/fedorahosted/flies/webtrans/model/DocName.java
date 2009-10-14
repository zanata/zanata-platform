package org.fedorahosted.flies.webtrans.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocName implements IsSerializable {
	private final String id;
	private final String name;
	private final String path;

	public DocName(String id, String name, String path) {
		this.id = id;
		this.name = name;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	
}
