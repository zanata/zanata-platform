package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.LocaleId;

import net.customware.gwt.dispatch.shared.Action;

public class GetEventsAction implements Action<GetEventsResult> {
	
	private static final long serialVersionUID = 1L;

	private int offset;
	private long projectContainerId;
	private LocaleId localeId;
	
	@SuppressWarnings("unused")
	private GetEventsAction() {
	}
	
	public GetEventsAction(long projectContainerId, LocaleId localeId, int offset) {
		this.offset = offset;
		this.projectContainerId = projectContainerId;
		this.localeId = localeId;
	}
	
	public int getOffset() {
		return offset;
	}

	public long getProjectContainerId() {
		return projectContainerId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}
}
