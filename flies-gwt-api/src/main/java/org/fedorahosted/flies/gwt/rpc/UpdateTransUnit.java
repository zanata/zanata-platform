package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class UpdateTransUnit implements DispatchAction<UpdateTransUnitResult> {
	
	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private String content;
	private LocaleId localeId;
	private ContentState contentState;
	
	@SuppressWarnings("unused")
	private UpdateTransUnit() {
	}
	
	public UpdateTransUnit(TransUnitId transUnitId, LocaleId localeId, String content, ContentState contentState) {
		this.transUnitId = transUnitId;
		this.localeId = localeId;
		this.content = content;
		this.contentState = contentState;
	}
	
	public String getContent() {
		return content;
	}
	
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}

	public ContentState getContentState() {
		return contentState;
	}
}
