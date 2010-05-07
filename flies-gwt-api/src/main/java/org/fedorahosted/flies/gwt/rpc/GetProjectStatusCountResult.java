package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectIterationId;

public class GetProjectStatusCountResult implements Result {
	private static final long serialVersionUID = 1L;
	
	private ArrayList<DocumentStatus> status;
	
	@SuppressWarnings("unused")
	private GetProjectStatusCountResult() {
	}
	
	public GetProjectStatusCountResult(ArrayList<DocumentStatus> status) {
		this.status = status;
	}
	
	public ArrayList<DocumentStatus> getStatus() {
		return status;
	}
		
}
