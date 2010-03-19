package org.fedorahosted.flies.webtrans.server;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.ProjectContainerId;
import org.fedorahosted.flies.gwt.rpc.EditingTranslationAction;
import org.fedorahosted.flies.gwt.rpc.EditingTranslationResult;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.security.FliesIdentity;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.EditTransUnitHandler")
@Scope(ScopeType.STATELESS)
public class EditTransUnitHandler implements ActionHandler<EditingTranslationAction, EditingTranslationResult> {

	@Logger Log log;
	
	@In Session session;
	
	@In TranslationWorkspaceManager translationWorkspaceManager;
	
	@Override
	public EditingTranslationResult execute(EditingTranslationAction action, ExecutionContext context)
			throws ActionException {
		FliesIdentity.instance().checkLoggedIn();
		HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
		
		ProjectContainerId projectContainerId = new ProjectContainerId(hTextFlow.getDocument().getProject().getId());
		
		TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(
				action.getWorkspaceId() );
		
		//If TransUnit is not editing, you can start editing now.
//		if(!workspace.containTransUnit(action.getTransUnitId()) && action.getEditState().equals(EditState.StartEditing)) {
//			workspace.addTransUnit(action.getTransUnitId(),action.getSessionId());
//		}
		
		//If TransUnit is editing by some else, you will be noticed. 
//		if (workspace.containTransUnit(action.getTransUnitId()) &&
//			!workspace.getTransUnitStatus(action.getTransUnitId()).equals(action.getSessionId()) && action.getEditState().equals(EditState.StartEditing)) {
//			
//			String sessionId = workspace.getTransUnitStatus(action.getTransUnitId());
//			TransUnitEditing event = new TransUnitEditing(
//					new DocumentId(hTextFlow.getDocument().getId()), action.getTransUnitId(), sessionId);
//			workspace.publish(event);
//		}
		
		//If TransUnit is editing by you, you can stop editing. 
//		if (workspace.containTransUnit(action.getTransUnitId()) &&
//				workspace.getTransUnitStatus(action.getTransUnitId()).equals(action.getSessionId()) && action.getEditState().equals(EditState.StopEditing)){
//			
//			workspace.removeTransUnit(action.getTransUnitId(), action.getSessionId());
//		}
		
		return new EditingTranslationResult(true);
	}

	@Override
	public Class<EditingTranslationAction> getActionType() {
		return EditingTranslationAction.class;
	}

	@Override
	public void rollback(EditingTranslationAction action, EditingTranslationResult result,
			ExecutionContext context) throws ActionException {
	}
}
