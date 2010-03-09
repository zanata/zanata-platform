package org.fedorahosted.flies.webtrans.server;

import java.util.ArrayList;
import java.util.Collection;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.core.dao.DocumentDAO;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetProjectStatusCountHandler")
@Scope(ScopeType.STATELESS)
public class GetProjectStatusCountHandler implements ActionHandler<GetProjectStatusCount, GetProjectStatusCountResult> {

		@Logger Log log;
		
		@In Session session;
		
		@In
		ProjectContainerDAO projectContainerDAO;
		@In
		DocumentDAO documentDAO;

		@In TranslationWorkspaceManager translationWorkspaceManager;
		
		@Override
		public GetProjectStatusCountResult execute(GetProjectStatusCount action,
				ExecutionContext context) throws ActionException {
			
			FliesIdentity.instance().checkLoggedIn();
			
			ProjectContainerId containerId = action.getProjectContainerId();
			log.info("Fetching Doc Status List for {0}", containerId);
			ArrayList<DocumentStatus> docliststatus = new ArrayList<DocumentStatus>(); 
			HProjectContainer hProjectContainer = projectContainerDAO.getById(containerId.getId());
			
			Collection<HDocument> hDocs = hProjectContainer.getDocuments().values();
			for (HDocument hDoc : hDocs) {
				DocumentId docId = new DocumentId(hDoc.getId());
								
				TranslationStatistics stat = documentDAO.getStatistics(docId.getValue(), action.getLocaleId() );
				
				DocumentStatus docstatus = new DocumentStatus(docId, stat.getNew(),stat.getNeedReview(), stat.getApproved());
				docliststatus.add(docstatus);
			}
						
			TranslationWorkspace workspace = translationWorkspaceManager.getWorkspace(action.getProjectContainerId().getId(), action.getLocaleId() );
			
			log.info("Returning Doc Status List for {0}: {1} elements", containerId, docliststatus.size());

			return new GetProjectStatusCountResult(action.getProjectContainerId(), docliststatus);

		}

		@Override
		public Class<GetProjectStatusCount> getActionType() {
			// TODO Auto-generated method stub
			return GetProjectStatusCount.class;
		}

		@Override
		public void rollback(GetProjectStatusCount action, GetProjectStatusCountResult result,
				ExecutionContext context) throws ActionException {
		}

}
