package org.fedorahosted.flies.webtrans.client;

import net.customware.gwt.dispatch.client.DispatchAsync;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.webtrans.client.auth.LoginResult;
import org.fedorahosted.flies.webtrans.client.rpc.CachingDispatchAsync;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class WorkspaceContext {
	private final ProjectContainerId projectContainerId;
	private final LocaleId localeId;

	private String workspaceName;
	private String localeName;

	private final DispatchAsync dispatcher;
	
	@Inject
	public WorkspaceContext(CachingDispatchAsync dispatcher) {
		this.projectContainerId = findProjectContainerId();
		this.localeId = findLocaleId();
		this.dispatcher = dispatcher;
	}

	private static LocaleId findLocaleId() {
		String localeId = Window.Location.getParameter("localeId");
		return localeId == null ? null : new LocaleId(localeId);
	}
	
	private static ProjectContainerId findProjectContainerId() {
		String projContainerId = Window.Location.getParameter("projContainerId");
		if(projContainerId == null)
			return null;
		try{
			int id = Integer.parseInt(projContainerId);
			return new ProjectContainerId(id);
		}
		catch(NumberFormatException nfe){
			return null;
		}
	}
	
	public void validateWorkspace(final LoginResult callback) {
		dispatcher.execute(new ActivateWorkspaceAction(projectContainerId, localeId), new AsyncCallback<ActivateWorkspaceResult>() {
			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure();
			}
			@Override
			public void onSuccess(ActivateWorkspaceResult result) {
				setWorkspaceName(result.getWorkspaceName());
				setLocaleName(result.getLocaleName());
				callback.onSuccess();
			}
		});
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}

	public LocaleId getLocaleId() {
		return localeId; 
	}
	
	public boolean isValid() {
		//return workspaceName != null && localeName != null;
		return true;
	}
	
	private void setWorkspaceName(String workspaceName) {
		this.workspaceName = workspaceName;
	}
	
	public String getWorkspaceName() {
		return workspaceName;
	}
	
	private void setLocaleName(String localeName) {
		this.localeName = localeName;
	}
	
	public String getLocaleName() {
		return localeName;
	}
}