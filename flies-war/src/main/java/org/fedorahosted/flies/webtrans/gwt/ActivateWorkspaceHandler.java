package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.PersonId;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.gwt.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.gwt.rpc.EnterWorkspace;
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
		
		HProjectContainer hProjectContainer = (HProjectContainer) session.get(HProjectContainer.class, action.getProjectContainerId().getId());
		
		//Send EnterWorkspace event to clients
		EnterWorkspace event = new EnterWorkspace(new PersonId(FliesIdentity.instance().getPrincipal().getName()));
//		EnterWorkspace event = GWT.create(EnterWorkspace.class);
//		event.setPersonId(new PersonId(FliesIdentity.instance().getPrincipal().getName()));
		workspace.publish(event);
		
		String iterationName = (String)session.createQuery(
				"select it.name " +
				"from HProjectIteration it " +
				"where it.container.id = :containerId "
				)
				.setParameter("containerId", hProjectContainer.getId())
				.uniqueResult();
		
		String projectName = (String)session.createQuery(
				"select it.project.name " +
				"from HProjectIteration it " +
				"where it.container.id = :containerId "
				)
				.setParameter("containerId", hProjectContainer.getId())
				.uniqueResult();

		return new ActivateWorkspaceResult(projectName+" "+iterationName, workspace.getLocale().toString());
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