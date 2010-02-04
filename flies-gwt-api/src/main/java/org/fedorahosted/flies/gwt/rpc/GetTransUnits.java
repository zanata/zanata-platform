package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.common.LocaleId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class GetTransUnits implements DispatchAction<GetTransUnitsResult> {

	private static final long serialVersionUID = 1L;

	private int offset;
	private int count;
	private DocumentId documentId;
	private LocaleId localeId;

	@SuppressWarnings("unused")
	private GetTransUnits(){
	}
	
	public GetTransUnits(DocumentId id, LocaleId localeId, int offset, int count) {
		this.documentId = id;
		this.localeId = localeId;
		this.offset = offset;
		this.count = count;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public DocumentId getDocumentId() {
		return documentId;
	}
	
	public void setDocumentId(DocumentId documentId) {
		this.documentId = documentId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public void setLocaleId(LocaleId localeId) {
		this.localeId = localeId;
	}
}
