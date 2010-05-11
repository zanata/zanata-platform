package org.fedorahosted.flies.webtrans.shared.model;

import java.io.Serializable;

import org.fedorahosted.flies.common.LocaleId;

public class TermEntry implements Serializable{

	private static final long serialVersionUID = -5265338285568364303L;
	
	private LocaleId localeid;
	private String term;
	private String comment;
	
	public TermEntry() {
		
	}
	
	public TermEntry(String term, String comment) {
		this.term = term;
		this.comment = comment;
	}
	
	public void setLocaleid(LocaleId localeid) {
		this.localeid = localeid;
	}
	
	public LocaleId getLocaleid() {
		return localeid;
	}
	
	public void setTerm(String term) {
		this.term = term;
	}
	
	public String getTerm() {
		return term;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getComment() {
		return comment;
	}

}
