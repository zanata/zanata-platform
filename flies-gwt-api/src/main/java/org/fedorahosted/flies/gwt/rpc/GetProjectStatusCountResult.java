package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.model.DocumentStatus;

public class GetProjectStatusCountResult implements SequenceResult {
	private static final long serialVersionUID = 1L;
	
	private ProjectContainerId projectContainerId;
	private ArrayList<DocumentStatus> status;
	private int offset;
	
	@SuppressWarnings("unused")
	private GetProjectStatusCountResult() {
	}
	
	public GetProjectStatusCountResult(ProjectContainerId projectContainerId, ArrayList<DocumentStatus> status, int offset) {
		this.projectContainerId = projectContainerId;
		this.status = status;
		this.offset = offset;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}
	
	public ArrayList<DocumentStatus> getStatus() {
		return status;
	}
	
	@Override
	public int getSequence() {
		return offset;
	}
	
}
