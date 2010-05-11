package org.fedorahosted.flies.webtrans.shared.model;

import java.io.Serializable;

public class Concept implements Serializable{

	private static final long serialVersionUID = 7816696203207673401L;
	
	private TermEntry entry;
	private String term;
	private String desc;
	private String comment;
	
	public Concept() {
		
	}

	public Concept(String term, String desc, String comment, TermEntry entry) {
		this.term = term;
		this.desc = desc;
		this.comment = comment;
		this.entry = entry;		
		
	}
	
	public void setEntry(TermEntry entry) {
		this.entry = entry;
	}

	public TermEntry getEntry() {
		return entry;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public String getTerm() {
		return term;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDesc() {
		return desc;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getComment() {
		return comment;
	}

}
