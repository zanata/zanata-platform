package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.common.WorkspaceId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class ExitWorkspaceAction extends AbstractWorkspaceAction<ExitWorkspaceResult>{

	private static final long serialVersionUID = 1L;
	
	private PersonId personId;
	
	@SuppressWarnings("unused")
	private ExitWorkspaceAction() {
	}
	
	public ExitWorkspaceAction(PersonId personId) {
		this.personId = personId;
	}
	
	public PersonId getPersonId() {
		return personId;
	}

}
