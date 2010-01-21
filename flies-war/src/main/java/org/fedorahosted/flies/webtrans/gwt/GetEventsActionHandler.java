package org.fedorahosted.flies.webtrans.gwt;

import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.rpc.GetEventsAction;
import org.fedorahosted.flies.gwt.rpc.GetEventsResult;
import org.fedorahosted.flies.repository.util.TranslationStatistics;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.GetEventsActionHandler")
@Scope(ScopeType.STATELESS)
public class GetEventsActionHandler implements ActionHandler<GetEventsAction, GetEventsResult> {

	@Logger Log log;
	
	@In Session session;
	
	@In TranslationWorkspaceManager translationWorkspaceManager;

	@Override
	public GetEventsResult execute(GetEventsAction action,
			ExecutionContext context) throws ActionException {

		TranslationWorkspace translationWorkspace = 
			translationWorkspaceManager.getOrRegisterWorkspace(action.getProjectContainerId(), 
					action.getLocaleId());
		
		return new GetEventsResult( translationWorkspace.getEventsSince(action.getOffset()));
	}

	@Override
	public Class<GetEventsAction> getActionType() {
		return GetEventsAction.class;
	}

	@Override
	public void rollback(GetEventsAction action, GetEventsResult result,
			ExecutionContext context) throws ActionException {
	}
}
