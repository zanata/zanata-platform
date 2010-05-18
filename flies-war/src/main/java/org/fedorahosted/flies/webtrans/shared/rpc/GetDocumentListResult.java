package org.fedorahosted.flies.webtrans.shared.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.webtrans.shared.model.DocumentInfo;
import org.fedorahosted.flies.webtrans.shared.model.ProjectIterationId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocumentListResult implements Result {

	private static final long serialVersionUID = 1L;

	private ProjectIterationId projectIterationId;
	private ArrayList<DocumentInfo> documents;

	@SuppressWarnings("unused")
	private GetDocumentListResult()	{
	}
	
	public GetDocumentListResult(ProjectIterationId projectIterationId, ArrayList<DocumentInfo> documents) {
		this.projectIterationId = projectIterationId;
		this.documents = documents;
	}
	
	public ArrayList<DocumentInfo> getDocuments() {
		return documents;
	}
	
	public ProjectIterationId getProjectIterationId() {
		return projectIterationId;
	}	
	
}
