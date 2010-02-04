package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetCommentsAction implements DispatchAction<GetCommentsResult> {

	private static final long serialVersionUID = 1L;

	private TransUnitId transUnitId;
	private LocaleId localeId;

	@SuppressWarnings("unused")
	private GetCommentsAction(){
	}
	
	public GetCommentsAction(TransUnitId transUnitId, LocaleId localeId) {
		this.transUnitId = transUnitId;
		this.localeId = localeId;
	}

	public TransUnitId getTransUnitId() {
		return transUnitId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}
}
