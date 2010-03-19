package org.fedorahosted.flies.gwt.common;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import de.novanic.eventservice.client.event.domain.Domain;

public class WorkspaceContext {

	private WorkspaceId workspaceId;
	private String workspaceName;
	private String localeName;
	private Domain domain;
	
	public WorkspaceContext(WorkspaceId workspaceId, String workspaceName, String localeName, Domain domain) {
		this.workspaceId = workspaceId;
		this.workspaceName = workspaceName;
		this.localeName = localeName;
		this.domain = domain;
	}
	
	@Override
	public String toString() {
		return workspaceId.toString();
	}

	public WorkspaceId getWorkspaceId() {
		return workspaceId;
	}
	
	public String getWorkspaceName() {
		return workspaceName;
	}
	
	public String getLocaleName() {
		return localeName;
	}
	
	public Domain getDomain() {
		return domain;
	}
}