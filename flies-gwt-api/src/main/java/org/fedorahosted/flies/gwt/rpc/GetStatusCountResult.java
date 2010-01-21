package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;

public class GetStatusCountResult implements SequenceResult {

	private static final long serialVersionUID = 1L;
	
	private DocumentId documentId;
	private long untranslated;
	private long fuzzy;
	private long translated;
	private int sequence;
	
	@SuppressWarnings("unused")
	private GetStatusCountResult()	{
	}
	
	public GetStatusCountResult(DocumentId documentId, long untranslated, long fuzzy, long translated, int sequence) {
		this.documentId = documentId;
		this.untranslated = untranslated;
		this.fuzzy = fuzzy;
		this.translated = translated;
		this.sequence = sequence;
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
	public int getSequence() {
		return sequence;
	}
	
}
