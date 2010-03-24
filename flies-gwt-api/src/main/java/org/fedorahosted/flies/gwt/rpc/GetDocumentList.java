package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocumentList implements DispatchAction<GetDocumentListResult> {

	private static final long serialVersionUID = 1L;

	private ProjectContainerId projectContainerId;

	@SuppressWarnings("unused")
	private GetDocumentList(){
	}
	
	public GetDocumentList(ProjectContainerId id) {
		this.projectContainerId = id;
	}

	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}
	
	public void setProjectContainerId(ProjectContainerId projectContainerId) {
		this.projectContainerId = projectContainerId;
	}
	
}
