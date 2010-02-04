package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class ExitWorkspaceAction implements DispatchAction<ExitWorkspaceResult>{

	private static final long serialVersionUID = 1L;
	
	private LocaleId localeId;
	private ProjectContainerId projectContainerId;
	private PersonId personId;
	
	@SuppressWarnings("unused")
	private ExitWorkspaceAction() {

	}

	public ExitWorkspaceAction(ProjectContainerId projectContainerId, LocaleId localeId, PersonId personId) {
		this.localeId = localeId;
		this.projectContainerId = projectContainerId;
		this.personId = personId;
	}

	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

	public void setPersonId(PersonId personId) {
		this.personId = personId;
	}

	public PersonId getPersonId() {
		return personId;
	}
	
	

}
