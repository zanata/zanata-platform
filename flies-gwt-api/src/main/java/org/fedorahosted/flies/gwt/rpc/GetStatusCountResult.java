package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;

public class GetStatusCountResult implements OffsetResult {

	private static final long serialVersionUID = 1L;
	
	private DocumentId documentId;
	private long untranslated;
	private long fuzzy;
	private long translated;
	private int offset;
	
	@SuppressWarnings("unused")
	private GetStatusCountResult()	{
	}
	
	public GetStatusCountResult(DocumentId documentId, long untranslated, long fuzzy, long translated, int offset) {
		this.documentId = documentId;
		this.untranslated = untranslated;
		this.fuzzy = fuzzy;
		this.translated = translated;
		this.offset = offset;
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
	
	@Override
	public int getOffset() {
		return offset;
	}
	
}
