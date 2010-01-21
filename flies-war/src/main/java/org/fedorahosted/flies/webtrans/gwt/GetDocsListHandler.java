package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetDocsList;
import org.fedorahosted.flies.gwt.rpc.GetDocsListResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.security.FliesIdentity;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetDocsListHandler")
@Scope(ScopeType.STATELESS)
public class GetDocsListHandler implements ActionHandler<GetDocsList, GetDocsListResult> {

	@Logger Log log;
	
	@In
	ProjectContainerDAO projectContainerDAO;

	
	@Override
	public GetDocsListResult execute(GetDocsList action, ExecutionContext context)
			throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();
		
		ProjectContainerId containerId = action.getProjectContainerId();
		ArrayList<DocName> docs = new ArrayList<DocName>(); 
		HProjectContainer hProjectContainer = projectContainerDAO.getById(containerId.getId());
		Collection<HDocument> hDocs = hProjectContainer.getDocuments().values();
		for (HDocument hDoc : hDocs) {
			DocumentId docId = new DocumentId(hDoc.getId());
			DocName docName = new DocName(docId, hDoc.getName(), hDoc.getPath());
			docs.add(docName);
		}
		return new GetDocsListResult(containerId, docs);
	}

	@Override
	public Class<GetDocsList> getActionType() {
		return GetDocsList.class;
	}

	@Override
	public void rollback(GetDocsList action, GetDocsListResult result,
			ExecutionContext context) throws ActionException {
	}
	
}