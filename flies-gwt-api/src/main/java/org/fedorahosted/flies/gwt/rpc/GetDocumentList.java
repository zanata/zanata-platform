package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.gwt.model.ProjectIterationId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocumentList implements DispatchAction<GetDocumentListResult> {

	private static final long serialVersionUID = 1L;

	private ProjectIterationId projectIterationId;

	@SuppressWarnings("unused")
	private GetDocumentList(){
	}
	
	public GetDocumentList(ProjectIterationId id) {
		this.projectIterationId = id;
	}

	public ProjectIterationId getProjectIterationId() {
		return projectIterationId;
	}
	
	public void setProjectContainerId(ProjectIterationId projectIterationId) {
		this.projectIterationId = projectIterationId;
	}
	
}
