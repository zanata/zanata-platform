package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class UpdateTransUnit implements Action<UpdateTransUnitResult>, IsSerializable {
	
	private static final long serialVersionUID = -3400992459393494862L;

	private TransUnitId transUnitId;
	private String content;
	private LocaleId localeId;
	
	@SuppressWarnings("unused")
	private UpdateTransUnit() {
	}
	
	public UpdateTransUnit(TransUnitId transUnitId, LocaleId localeId, String content) {
		this.transUnitId = transUnitId;
		this.localeId = localeId;
		this.content = content;
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

}
