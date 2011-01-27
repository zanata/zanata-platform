package net.openl10n.flies.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import net.openl10n.flies.security.FliesIdentity;
import net.openl10n.flies.webtrans.server.ActionHandlerFor;
import net.openl10n.flies.webtrans.server.TranslationWorkspace;
import net.openl10n.flies.webtrans.server.TranslationWorkspaceManager;
import net.openl10n.flies.webtrans.shared.rpc.ExitWorkspace;
import net.openl10n.flies.webtrans.shared.rpc.ExitWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ExitWorkspaceResult;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;

@Name("webtrans.gwt.ExitWorkspaceHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(ExitWorkspaceAction.class)
public class ExitWorkspaceHandler extends AbstractActionHandler<ExitWorkspaceAction, ExitWorkspaceResult>
{

   @Logger
   Log log;

   @In
   Session session;

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public ExitWorkspaceResult execute(ExitWorkspaceAction action, ExecutionContext context) throws ActionException
   {

      FliesIdentity.instance().checkLoggedIn();

      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());

      // Send ExitWorkspace event to client
      if (workspace.removeTranslator(action.getPersonId()))
      {
         // Send GWT Event to client to update the userlist
         ExitWorkspace event = new ExitWorkspace(action.getPersonId());
         workspace.publish(event);
      }

      return new ExitWorkspaceResult(action.getPersonId().toString());
   }

   @Override
   public void rollback(ExitWorkspaceAction action, ExitWorkspaceResult result, ExecutionContext context) throws ActionException
   {
   }
}
