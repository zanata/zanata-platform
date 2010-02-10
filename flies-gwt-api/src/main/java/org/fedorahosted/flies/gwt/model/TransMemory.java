package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

public class TransMemory implements Serializable{

	private static final long serialVersionUID = -7381018377520206564L;

	private String source;
	private String memory;
	private String docID;
	private int relevanceScore;
	// TODO we should probably include transunit id too

	public TransMemory() {
	}

	public TransMemory(String source, String memory, String documentPath, int relevanceScore) {
		this.source = source;
		this.memory = memory;
		this.docID = documentPath;
		this.relevanceScore = relevanceScore;
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

	public void setRelevanceScore(int relevanceScore) {
		this.relevanceScore = relevanceScore;
	}

	public int getRelevanceScore() {
		return relevanceScore;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}
}
