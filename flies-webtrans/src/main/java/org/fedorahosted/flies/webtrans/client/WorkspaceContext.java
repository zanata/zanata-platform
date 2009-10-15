package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import com.google.gwt.user.client.Window;

public class WorkspaceContext {
	private ProjectContainerId projectContainerId;
	private String localeId;

	public WorkspaceContext() {
		try {
			String projContainerId = Window.Location.getParameter("projContainerId");
			projectContainerId = new ProjectContainerId(Integer.valueOf(projContainerId));
			localeId = Window.Location.getParameter("localeId");
		} catch (Exception e) {
			// leave them as null
		} 
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

	public String getLocaleId() {
		return localeId; 
	}
	
	public boolean isValid() {
		// TODO login/enter the workspace context, validate these ids
		return projectContainerId != null && localeId != null;
	}
}
