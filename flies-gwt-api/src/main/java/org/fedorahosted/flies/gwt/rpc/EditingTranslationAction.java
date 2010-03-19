package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.EditState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.auth.SessionId;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.TransUnitId;

public class EditingTranslationAction implements WorkspaceAction<EditingTranslationResult> {
	
	private static final long serialVersionUID = 1L;

	private WorkspaceId workspaceId;
	private TransUnitId transUnitId;
	private EditState editState;
	private SessionId sessionId;
	
	@SuppressWarnings("unused")
	private EditingTranslationAction() {
	}
	
	public EditingTranslationAction(WorkspaceId workspaceId, TransUnitId transUnitId, SessionId sessionid, EditState editState) {
		this.workspaceId = workspaceId;
		this.transUnitId = transUnitId;
		this.editState = editState;
		this.sessionId = sessionid;
	}
	
	public TransUnitId getTransUnitId() {
		return transUnitId;
	}

	@Override
	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}

	public EditState getEditState() {
		return editState;
	}
	
	public SessionId getSessionId() {
		return sessionId;
	}
}