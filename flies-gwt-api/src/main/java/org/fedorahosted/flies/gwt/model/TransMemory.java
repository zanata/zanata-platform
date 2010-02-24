package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

public class TransMemory implements Serializable{

	private static final long serialVersionUID = -7381018377520206564L;

	private String source;
	private String memory;

	private String sourceComment;
	// TODO we should probably include transunit id too (useful when we support browser history for TUs)
	// TODO include obsolete flag and show this info to the user

	private String targetComment;

	private String docID;

	private long projectContainer;

	public TransMemory() {
	}

	@Deprecated
	public TransMemory(String source, String memory, String documentPath) {
		this(source, memory, "", "", documentPath, 0);
	}
	
	public TransMemory(String source, String memory, String sourceComment, String targetComment, String documentPath, long projectContainer) {
		this.source = source;
		this.memory = memory;
		this.sourceComment = sourceComment;
		this.targetComment = targetComment;
		this.docID = documentPath;
		this.projectContainer = projectContainer;
	}
	
	public void setDocID(String documentPath) {
		this.docID = documentPath;
	}

	public String getDocID() {
		return docID;
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
