package org.fedorahosted.flies.webtrans.server;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.EnterWorkspace;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspace;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ExitWorkspaceResult;
import org.fedorahosted.flies.repository.model.HProjectContainer;
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


@Name("webtrans.gwt.ExitWorkspaceHandler")
@Scope(ScopeType.STATELESS)
public class ExitWorkspaceHandler implements ActionHandler<ExitWorkspaceAction, ExitWorkspaceResult> {
@Logger Log log;
	
	@In Session session;
	
	@In TranslationWorkspaceManager translationWorkspaceManager;

	@Override
	public ExitWorkspaceResult execute(ExitWorkspaceAction action, ExecutionContext context)
			throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();

		TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getProjectContainerId().getId(), action.getLocaleId());
			
		//Send ExitWorkspace event to client 
		if(workspace.removeTranslator(action.getPersonId())) {
			//Send GWT Event to client to update the userlist
			ExitWorkspace event = new ExitWorkspace(action.getPersonId());
			workspace.publish(event);
		}

		return new ExitWorkspaceResult(action.getPersonId().toString());
	}

	@Override
	public Class<ExitWorkspaceAction> getActionType() {
		return ExitWorkspaceAction.class;
	}

	@Override
	public void rollback(ExitWorkspaceAction action, ExitWorkspaceResult result,
			ExecutionContext context) throws ActionException {
	}
}
