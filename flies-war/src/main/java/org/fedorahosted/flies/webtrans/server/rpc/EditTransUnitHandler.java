package org.fedorahosted.flies.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.fedorahosted.flies.model.HTextFlow;
import org.fedorahosted.flies.security.FliesIdentity;
import org.fedorahosted.flies.webtrans.server.ActionHandlerFor;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspace;
import org.fedorahosted.flies.webtrans.server.TranslationWorkspaceManager;
import org.fedorahosted.flies.webtrans.shared.rpc.EditingTranslationAction;
import org.fedorahosted.flies.webtrans.shared.rpc.EditingTranslationResult;
import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.EditTransUnitHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(EditingTranslationAction.class)
public class EditTransUnitHandler extends AbstractActionHandler<EditingTranslationAction, EditingTranslationResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public EditingTranslationResult execute(EditingTranslationAction action, ExecutionContext context) throws ActionException
   {
      FliesIdentity.instance().checkLoggedIn();
      HTextFlow hTextFlow = (HTextFlow) session.get(HTextFlow.class, action.getTransUnitId().getValue());

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());

      // If TransUnit is not editing, you can start editing now.
      // if(!workspace.containTransUnit(action.getTransUnitId()) &&
      // action.getEditState().equals(EditState.StartEditing)) {
      // workspace.addTransUnit(action.getTransUnitId(),action.getSessionId());
      // }

      // If TransUnit is editing by some else, you will be noticed.
      // if (workspace.containTransUnit(action.getTransUnitId()) &&
      // !workspace.getTransUnitStatus(action.getTransUnitId()).equals(action.getSessionId())
      // && action.getEditState().equals(EditState.StartEditing)) {
      //			
      // String sessionId =
      // workspace.getTransUnitStatus(action.getTransUnitId());
      // TransUnitEditing event = new TransUnitEditing(
      // new DocumentId(hTextFlow.getDocument().getId()),
      // action.getTransUnitId(), sessionId);
      // workspace.publish(event);
      // }

      // If TransUnit is editing by you, you can stop editing.
      // if (workspace.containTransUnit(action.getTransUnitId()) &&
      // workspace.getTransUnitStatus(action.getTransUnitId()).equals(action.getSessionId())
      // && action.getEditState().equals(EditState.StopEditing)){
      //			
      // workspace.removeTransUnit(action.getTransUnitId(),
      // action.getSessionId());
      // }

      return new EditingTranslationResult(true);
   }

   @Override
   public void rollback(EditingTranslationAction action, EditingTranslationResult result, ExecutionContext context) throws ActionException
   {
   }
}
