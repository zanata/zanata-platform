package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetProjectStatusCount implements WorkspaceAction<GetProjectStatusCountResult>{
	
	private static final long serialVersionUID = -143162507696820592L;

	private LocaleId localeId;
	private ProjectContainerId projectContainerId;

	@SuppressWarnings("unused")
	private GetProjectStatusCount(){
	}
	
	public GetProjectStatusCount(ProjectContainerId projectContainerId, LocaleId localeId) {
		this.localeId = localeId;
		this.projectContainerId = projectContainerId;
	}

	@Override
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	@Override
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

}
