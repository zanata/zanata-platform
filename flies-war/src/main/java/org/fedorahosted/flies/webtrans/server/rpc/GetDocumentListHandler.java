package org.fedorahosted.flies.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.gwt.model.DocumentInfo;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetDocumentList;
import org.fedorahosted.flies.gwt.rpc.GetDocumentListResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetDocsListHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetDocumentList.class)
public class GetDocumentListHandler extends AbstractActionHandler<GetDocumentList, GetDocumentListResult> {

	@Logger Log log;
	
	@In
	ProjectContainerDAO projectContainerDAO;

	
	@Override
	public GetDocumentListResult execute(GetDocumentList action, ExecutionContext context)
			throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();
		
		ProjectContainerId containerId = action.getProjectContainerId();
		ArrayList<DocumentInfo> docs = new ArrayList<DocumentInfo>(); 
		HProjectContainer hProjectContainer = projectContainerDAO.getById(containerId.getId());
		Collection<HDocument> hDocs = hProjectContainer.getDocuments().values();
		for (HDocument hDoc : hDocs) {
			DocumentId docId = new DocumentId(hDoc.getId());
			DocumentInfo doc = new DocumentInfo(docId, hDoc.getName(), hDoc.getPath());
			docs.add(doc);
		}
		return new GetDocumentListResult(containerId, docs);
	}

	@Override
	public void rollback(GetDocumentList action, GetDocumentListResult result,
			ExecutionContext context) throws ActionException {
	}
	
}