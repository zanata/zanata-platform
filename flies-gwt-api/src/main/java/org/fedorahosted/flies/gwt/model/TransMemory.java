package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

public class TransMemory implements Serializable{

	private static final long serialVersionUID = -7381018377520206564L;

	private String memory;
	private String source;

	public TransMemory() {
	}

	public TransMemory(String source, String memory) {
		this.source = source;
		this.memory = memory;
	}
	
	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getMemory() {
		return memory;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}
}
