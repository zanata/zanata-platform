package org.fedorahosted.flies.webtrans.gwt;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.gwt.model.DocumentId;
import org.fedorahosted.flies.gwt.rpc.EditingTranslationAction;
import org.fedorahosted.flies.gwt.rpc.EditingTranslationResult;
import org.fedorahosted.flies.gwt.rpc.TransUnitEditing;
import org.fedorahosted.flies.repository.model.HTextFlow;
import org.fedorahosted.flies.webtrans.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.TranslationWorkspaceManager;
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
		HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());
		
		TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(
				hTextFlow.getDocument().getProject().getId(), action.getLocaleId() );
		
		workspace.lockTransUnit(action.getTransUnitId());
		
		TransUnitEditing event = new TransUnitEditing(
				new DocumentId(hTextFlow.getDocument().getId()), action.getTransUnitId(), action.getEditState());
		
		
		workspace.publish(event);
		
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
