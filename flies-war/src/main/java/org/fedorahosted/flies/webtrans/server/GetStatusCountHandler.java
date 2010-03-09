package org.fedorahosted.flies.webtrans.server;

import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.core.model.StatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCount;
import org.fedorahosted.flies.gwt.rpc.GetStatusCountResult;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetStatusCountHandler")
@Scope(ScopeType.STATELESS)
public class GetStatusCountHandler implements ActionHandler<GetStatusCount, GetStatusCountResult> {

	@Logger Log log;
	
	@In Session session;

	@In TranslationWorkspaceManager translationWorkspaceManager;
	
	@Override
	public GetStatusCountResult execute(GetStatusCount action,
			ExecutionContext context) throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();
		
		List<StatusCount> stats = session.createQuery(
				"select new org.fedorahosted.flies.core.model.StatusCount(tft.state, count(tft)) " +
		        "from HTextFlowTarget tft where tft.textFlow.document.id = :id " +
		        "  and tft.locale = :locale "+ 
				"group by tft.state"
			).setParameter("id", action.getDocumentId().getValue())
			 .setParameter("locale", action.getLocaleId() )
			 .list();
		
		
		Long totalCount = (Long) session.createQuery("select count(tf) from HTextFlow tf where tf.document.id = :id")
			.setParameter("id", action.getDocumentId().getValue())
			.uniqueResult();
		
		TranslationStatistics stat = new TranslationStatistics();
		for(StatusCount count: stats){
			stat.set(count.status, count.count);
		}
		
		stat.set(ContentState.New, totalCount - stat.getApproved()-stat.getNeedReview());
		TranslationWorkspace workspace = translationWorkspaceManager.getWorkspace(action.getProjectContainerId().getId(), action.getLocaleId() );
		
		return new GetStatusCountResult(
				action.getDocumentId(), 
				stat.getNew(),stat.getNeedReview(), stat.getApproved());

	}

	@Override
	public Class<GetStatusCount> getActionType() {
		// TODO Auto-generated method stub
		return GetStatusCount.class;
	}

	@Override
	public void rollback(GetStatusCount action, GetStatusCountResult result,
			ExecutionContext context) throws ActionException {
	}
}
