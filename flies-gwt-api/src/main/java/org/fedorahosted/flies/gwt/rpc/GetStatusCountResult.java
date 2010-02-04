package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentId;

public class GetStatusCountResult implements Result {

	private static final long serialVersionUID = 1L;
	
	private DocumentId documentId;
	private long untranslated;
	private long fuzzy;
	private long translated;
	
	@SuppressWarnings("unused")
	private GetStatusCountResult()	{
	}
	
	public GetStatusCountResult(DocumentId documentId, long untranslated, long fuzzy, long translated) {
		this.documentId = documentId;
		this.untranslated = untranslated;
		this.fuzzy = fuzzy;
		this.translated = translated;
	}
	
	public DocumentId getDocumentId() {
		return documentId;
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
	
}
