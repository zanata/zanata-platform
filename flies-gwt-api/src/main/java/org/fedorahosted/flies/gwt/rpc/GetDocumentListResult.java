package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocumentInfo;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocumentListResult implements Result {

	private static final long serialVersionUID = 1L;

	private ProjectContainerId projectContainerId;
	private ArrayList<DocumentInfo> documents;

	@SuppressWarnings("unused")
	private GetDocumentListResult()	{
	}
	
	public GetDocumentListResult(ProjectContainerId projectContainerId, ArrayList<DocumentInfo> documents) {
		this.projectContainerId = projectContainerId;
		this.documents = documents;
	}
	
	public ArrayList<DocumentInfo> getDocuments() {
		return documents;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}	
	
}
