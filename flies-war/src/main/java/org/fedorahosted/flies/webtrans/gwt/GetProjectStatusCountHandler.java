package org.fedorahosted.flies.webtrans.gwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.core.dao.ProjectContainerDAO;
import org.fedorahosted.flies.core.model.StatusCount;
import org.fedorahosted.flies.gwt.model.DocName;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.DocumentStatus;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.repository.model.HDocument;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.fedorahosted.flies.rest.dto.TextFlowTarget.ContentState;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
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

		@In TranslationWorkspaceManager translationWorkspaceManager;
		
		@Override
		public GetProjectStatusCountResult execute(GetProjectStatusCount action,
				ExecutionContext context) throws ActionException {
			org.fedorahosted.flies.LocaleId fliesLocaleId = new org.fedorahosted.flies.LocaleId(action.getLocaleId().getValue());		
			
			ProjectContainerId containerId = action.getProjectContainerId();
			log.info("Fetching Docs List for {0}", containerId);
			ArrayList<DocumentStatus> docliststatus = new ArrayList<DocumentStatus>(); 
			HProjectContainer hProjectContainer = projectContainerDAO.getById(containerId.getId());
			Collection<HDocument> hDocs = hProjectContainer.getDocuments().values();
			for (HDocument hDoc : hDocs) {
				DocumentId docId = new DocumentId(hDoc.getId());
				
				List<StatusCount> stats = session.createQuery(
						"select new org.fedorahosted.flies.core.model.StatusCount(tft.state, count(tft)) " +
				        "from HTextFlowTarget tft where tft.textFlow.document.id = :id " +
				        "  and tft.locale = :locale "+ 
						"group by tft.state"
					).setParameter("id", docId.getValue())
					 .setParameter("locale", fliesLocaleId)
					 .list();
				
				
				Long totalCount = (Long) session.createQuery("select count(tf) from HTextFlow tf where tf.document.project.id = :id")
					.setParameter("id", action.getProjectContainerId().getId())
					.uniqueResult();
				
				TranslationStatistics stat = new TranslationStatistics();
				for(StatusCount count: stats){
					stat.set(count.status, count.count);
				}
				
				stat.set(ContentState.New, totalCount - stat.getNotApproved());
								
				DocumentStatus docstatus = new DocumentStatus(docId, stat.getNew(),stat.getFuzzyMatch()+stat.getForReview(), stat.getApproved());
				docliststatus.add(docstatus);
			}
			
			
			//LocaleId localeId = new LocaleId(action.getLocaleId().getValue());
			TranslationWorkspace workspace = translationWorkspaceManager.getWorkspace(action.getProjectContainerId().getId(), fliesLocaleId);
			
			return new GetProjectStatusCountResult(action.getProjectContainerId(), docliststatus, workspace.getSequence());

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
