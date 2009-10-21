package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentId;
import com.google.gwt.user.client.rpc.IsSerializable;

public class GetStatusCountResult implements Result, IsSerializable {

	private static final long serialVersionUID = 5021443732856834627L;
	
	private int untranslated;
	private int fuzzy;
	private int translated;

	@SuppressWarnings("unused")
	private GetStatusCountResult()	{
	}
	
	public GetStatusCountResult(DocumentId documentId, int untranslated, int fuzzy, int translated) {
		this.untranslated = untranslated;
		this.fuzzy = fuzzy;
		this.translated = translated;
	}
	
	public int getUntranslated() {
		return untranslated;
	}
	
	public int getFuzzy() {
		return fuzzy;
	}
	
	public int getTranslated() {
		return translated;
	}
	
}
