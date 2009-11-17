package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.auth.AuthenticationError;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
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
import org.jboss.seam.security.Identity;

@Name("webtrans.gwt.ActivateWorkspaceHandler")
@Scope(ScopeType.STATELESS)
public class ActivateWorkspaceHandler implements ActionHandler<ActivateWorkspaceAction, ActivateWorkspaceResult> {

	@Logger Log log;
	
	@In Session session;
	
	@In TranslationWorkspaceManager translationWorkspaceManager;

	@Override
	public ActivateWorkspaceResult execute(ActivateWorkspaceAction action, ExecutionContext context)
			throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();

		TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getProjectContainerId().getId(), action.getLocaleId());
		
		workspace.registerTranslator(AuthenticateHandler.retrieveSessionId(), AuthenticateHandler.retrievePersonId());
		
//		HProjectContainer hProjectContainer = (HProjectContainer) session.get(HProjectContainer.class, action.getProjectContainerId().getId());
		
		return new ActivateWorkspaceResult("My Project v 1.0", "My Language");
	}

	@Override
	public Class<ActivateWorkspaceAction> getActionType() {
		return ActivateWorkspaceAction.class;
	}

	@Override
	public void rollback(ActivateWorkspaceAction action, ActivateWorkspaceResult result,
			ExecutionContext context) throws ActionException {
	}
	
}