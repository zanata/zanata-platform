package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentId;
import com.google.gwt.user.client.rpc.IsSerializable;

public class GetStatusCountResult implements Result, IsSerializable {

	private static final long serialVersionUID = 5021443732856834627L;
	
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
