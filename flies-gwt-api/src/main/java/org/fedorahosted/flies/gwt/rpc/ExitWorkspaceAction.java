package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class ExitWorkspaceAction implements WorkspaceAction<ExitWorkspaceResult>{

	private static final long serialVersionUID = 1L;
	
	private WorkspaceId workspaceId;
	private PersonId personId;
	
	@SuppressWarnings("unused")
	private ExitWorkspaceAction() {

	}

	public ExitWorkspaceAction(WorkspaceId workspaceId, PersonId personId) {
		this.workspaceId = workspaceId;
		this.personId = personId;
	}

	@Override
	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}
	
	public void setPersonId(PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}
	
	

}
