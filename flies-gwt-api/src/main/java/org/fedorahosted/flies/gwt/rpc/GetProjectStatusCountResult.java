package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

public class GetProjectStatusCountResult implements Result {
	private static final long serialVersionUID = 1L;
	
	private ProjectContainerId projectContainerId;
	private ArrayList<DocumentStatus> status;
	
	@SuppressWarnings("unused")
	private GetProjectStatusCountResult() {
	}
	
	public GetProjectStatusCountResult(ProjectContainerId projectContainerId, ArrayList<DocumentStatus> status) {
		this.projectContainerId = projectContainerId;
		this.status = status;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}
	
	public ArrayList<DocumentStatus> getStatus() {
		return status;
	}
		
}
