package org.fedorahosted.flies.gwt.model;

import java.io.Serializable;

import org.fedorahosted.flies.common.ContentState;


public class DocumentStatus implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private long untranslated;
	private long fuzzy;
	private long translated;
	private DocumentId documentid;
	
	private DocumentStatus() {
	}
	
	public DocumentStatus(DocumentId id, long untranslated, long fuzzy, long translated) {
		this.documentid = id;
		this.untranslated = untranslated;
		this.fuzzy = fuzzy;
		this.translated = translated;
	}
	
	
	public long getUntranslated() {
		return untranslated;
	}
	
	public long getFuzzy() {
		return fuzzy;
	}
	
	public long getTranslated() {
		return translated;
	}
	
	public long getStatus(ContentState status) {
		switch(status) {
		case Approved:
			return translated;
		case NeedReview:
			return fuzzy;
		case New:
			return untranslated;
		}
		throw new RuntimeException("missing enum in switch statement");
	}
	
	public void setStatus(ContentState status, long value) {
		switch(status) {
		case Approved:
			translated = value;
			break;
		case NeedReview:
			fuzzy = value;
			break;
		case New:
			untranslated = value;
			break;
		}
	}
	
	public void setFuzzy(long fuzzy) {
		this.fuzzy = fuzzy;
	}
	
	public void setDocumentid(DocumentId documentid) {
		this.documentid = documentid;
	}
	
	public void setTranslated(long translated) {
		this.translated = translated;
	}
	
	public void setUntranslated(long untranslated) {
		this.untranslated = untranslated;
	}

	public DocumentId getDocumentid() {
		return documentid;
	}
}
