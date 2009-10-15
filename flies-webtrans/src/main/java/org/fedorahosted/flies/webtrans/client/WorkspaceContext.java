package org.fedorahosted.flies.webtrans.client;

import org.fedorahosted.flies.gwt.model.ProjectIterationId;

import com.google.gwt.user.client.Window;

public class WorkspaceContext {
	private ProjectIterationId projectIterationId;
	private String localeId;

	public WorkspaceContext() {
		String projIterId = Window.Location.getParameter("projIterId");
		projectIterationId = new ProjectIterationId(Integer.valueOf(projIterId));
		localeId = Window.Location.getParameter("localeId"); 
	}
	
	public ProjectIterationId getProjectIterationId() {
		return projectIterationId;
	}

	public String getLocaleId() {
		return localeId; 
	}
	
	public boolean isValid() {
		// TODO login/enter the workspace context
		return true;
	}
}
