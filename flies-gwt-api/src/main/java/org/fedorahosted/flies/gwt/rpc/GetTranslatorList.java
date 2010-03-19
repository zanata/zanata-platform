package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetTranslatorList implements WorkspaceAction<GetTranslatorListResult> {

	private static final long serialVersionUID = -4430737335471654451L;
	private WorkspaceId workspaceId;

	@SuppressWarnings("unused")
	private GetTranslatorList() {
	}
	
	public GetTranslatorList (WorkspaceId workspaceId) {
		this.workspaceId = workspaceId;
	}

	@Override
	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}
}

