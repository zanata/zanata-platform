package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.PersonId;

public class EnterWorkspace implements SessionEventData, HasEnterWorkspaceData{
	private static final long serialVersionUID = 1L;

	private PersonId personId;
	
	@SuppressWarnings("unused")
	private EnterWorkspace() {

	}
	
	public EnterWorkspace(PersonId personId) {
		this.personId = personId;
	}

	@Override
	public PersonId getPersonId() {
		return personId;
	}

}


