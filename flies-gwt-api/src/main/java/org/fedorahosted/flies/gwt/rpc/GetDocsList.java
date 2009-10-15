package org.fedorahosted.flies.gwt.rpc;

import net.customware.gwt.dispatch.shared.Action;

import org.fedorahosted.flies.gwt.model.ProjectIterationId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocsList implements Action<GetDocsListResult>, IsSerializable {

	private static final long serialVersionUID = 1L;

	private ProjectIterationId projectIterationId;

	private GetDocsList(){
	}
	
	public GetDocsList(ProjectIterationId id) {
		this.projectIterationId = id;
	}

	public ProjectIterationId getProjectIterationId() {
		return projectIterationId;
	}
	
	public void setProjectIterationId(ProjectIterationId documentId) {
		this.projectIterationId = documentId;
	}
	
}
