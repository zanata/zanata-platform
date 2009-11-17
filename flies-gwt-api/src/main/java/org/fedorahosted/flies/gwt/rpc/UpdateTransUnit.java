package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class UpdateTransUnit implements Action<UpdateTransUnitResult> {
	
	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private String content;
	private LocaleId localeId;
	private TransUnitStatus status;
	
	@SuppressWarnings("unused")
	private UpdateTransUnit() {
	}
	
	public UpdateTransUnit(TransUnitId transUnitId, LocaleId localeId, String content, TransUnitStatus status) {
		this.transUnitId = transUnitId;
		this.localeId = localeId;
		this.content = content;
		this.status = status;
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

	public TransUnitStatus getStatus() {
		return status;
	}

}
