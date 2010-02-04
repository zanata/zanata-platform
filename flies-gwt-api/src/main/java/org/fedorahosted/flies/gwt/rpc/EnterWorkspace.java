package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.PersonId;
import org.jboss.errai.bus.server.annotations.ExposeEntity;

@ExposeEntity 
public class EnterWorkspace implements SessionEventData, HasEnterWorkspaceData{
	private static final long serialVersionUID = 1L;

	private PersonId personId;
	
	// for ExposeEntity
	public EnterWorkspace() {

	}
	
	public EnterWorkspace(PersonId personId) {
		this.personId = personId;
	}
	
	@Override
	public PersonId getPersonId() {
		return personId;
	}

	public void setPersonId(PersonId personId) {
		this.personId = personId;
	}
	
}


