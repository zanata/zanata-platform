package org.fedorahosted.flies.gwt.rpc;

import java.util.ArrayList;

import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.ProjectIterationId;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GetDocsListResult implements Result, IsSerializable {

	private static final long serialVersionUID = 1L;

	private ProjectIterationId projectIterationId;
	private ArrayList<DocName> docNames;

	private GetDocsListResult()	{
		
	}
	
	public GetDocsListResult(ProjectIterationId projectIterationId, ArrayList<DocName> units) {
		this.projectIterationId = projectIterationId;
		this.docNames = units;
	}
	
	public ArrayList<DocName> getDocNames() {
		return docNames;
	}
	
	public ProjectIterationId getProjectIterationId() {
		return projectIterationId;
	}	
	
}
