package org.fedorahosted.flies.webtrans.server;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.common.LocaleId;
import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.TransUnitUpdated;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnit;
import org.fedorahosted.flies.gwt.rpc.UpdateTransUnitResult;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.repository.model.HTextFlowTarget;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.UpdateTransUnitHandler")
@Scope(ScopeType.STATELESS)
public class UpdateTransUnitHandler implements ActionHandler<UpdateTransUnit, UpdateTransUnitResult> {

	@Logger Log log;
	
	@In Session session;
	
	@In TranslationWorkspaceManager translationWorkspaceManager;
	
	@Override
	public UpdateTransUnitResult execute(UpdateTransUnit action, ExecutionContext context)
			throws ActionException {
		
		FliesIdentity.instance().checkLoggedIn();
		log.info("Updating TransUnit {0}: locale {1}, state {2}, content '{3}'", action.getTransUnitId(), action.getWorkspaceId().getLocaleId(), action.getContentState(), action.getContent());
		
		HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
		HTextFlowTarget target = hTextFlow.getTargets().get( action.getWorkspaceId().getLocaleId() );
		ContentState prevStatus = ContentState.New;
		if(target == null) {
			target = new HTextFlowTarget(hTextFlow, action.getWorkspaceId().getLocaleId() );
			hTextFlow.getTargets().put(action.getWorkspaceId().getLocaleId() , target);
		}
		else{
			prevStatus = target.getState();
		}
		target.setState(action.getContentState());
		target.setContent(action.getContent());
		session.flush();
		
		TransUnitUpdated event = new TransUnitUpdated(
				new DocumentId(hTextFlow.getDocument().getId()), action.getTransUnitId(), prevStatus, action.getContentState() );
		
		ProjectContainerId projectContainerId = new ProjectContainerId(hTextFlow.getDocument().getProject().getId());
		
		TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(
				action.getWorkspaceId() );
		workspace.publish(event);
		
		return new UpdateTransUnitResult(true);
	}

	@Override
	public Class<UpdateTransUnit> getActionType() {
		return UpdateTransUnit.class;
	}

	@Override
	public void rollback(UpdateTransUnit action, UpdateTransUnitResult result,
			ExecutionContext context) throws ActionException {
	}
	
}