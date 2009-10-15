package org.fedorahosted.flies.gwt.rpc;

import org.fedorahosted.flies.gwt.model.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import com.google.gwt.user.client.rpc.IsSerializable;

import net.customware.gwt.dispatch.shared.Action;

public class GetWorkspaceContext implements Action<GetWorkspaceContextResult>, IsSerializable{
	
	private static final long serialVersionUID = 1647105522769097523L;

	private LocaleId localeId;
	private ProjectContainerId projectContainerId;
	
	@SuppressWarnings("unused")
	private GetWorkspaceContext() {
	}

	public GetWorkspaceContext(ProjectContainerId projectContainerId, LocaleId localeId) {
		this.localeId = localeId;
		this.projectContainerId = projectContainerId;
	}
	
	public LocaleId getLocaleId() {
		return localeId;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

}
