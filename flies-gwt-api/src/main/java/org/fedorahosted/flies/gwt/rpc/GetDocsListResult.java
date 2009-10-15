package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocsListResult implements Result, IsSerializable {

	private static final long serialVersionUID = 1L;

	private ProjectContainerId projectContainerId;
	private ArrayList<DocName> docNames;

	@SuppressWarnings("unused")
	private GetDocsListResult()	{
	}
	
	public GetDocsListResult(ProjectContainerId projectContainerId, ArrayList<DocName> units) {
		this.projectContainerId = projectContainerId;
		this.docNames = units;
	}
	
	public ArrayList<DocName> getDocNames() {
		return docNames;
	}
	
	public ProjectContainerId getProjectContainerId() {
		return projectContainerId;
	}	
	
}
