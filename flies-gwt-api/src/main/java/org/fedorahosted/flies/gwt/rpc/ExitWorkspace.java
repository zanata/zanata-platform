package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.PersonId;

public class ExitWorkspace implements SessionEventData, HasExitWorkspaceData{
	private static final long serialVersionUID = 1L;

	private PersonId personId;
	
	@SuppressWarnings("unused")
	private ExitWorkspace() {
		// TODO Auto-generated constructor stub
	}
	
	public ExitWorkspace(PersonId personId) {
		this.personId = personId;
	}

	@Override
	public PersonId getPersonId() {
		return personId;
	}


}
