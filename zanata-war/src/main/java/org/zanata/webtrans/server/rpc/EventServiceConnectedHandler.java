package org.zanata.webtrans.server.rpc;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;
import org.zanata.webtrans.shared.rpc.NoOpResult;

@Name("webtrans.gwt.EventServiceConnectedHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(EventServiceConnectedAction.class)
public class EventServiceConnectedHandler extends AbstractActionHandler<EventServiceConnectedAction, NoOpResult>
{

   @In
   TranslationWorkspaceManager translationWorkspaceManager;

   @Override
   public NoOpResult execute(EventServiceConnectedAction action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();
      TranslationWorkspace workspace = translationWorkspaceManager.getOrRegisterWorkspace(action.getWorkspaceId());
      workspace.onEventServiceConnected(action.getEditorClientId(), action.getConnectionId());
      return new NoOpResult();
   }

   @Override
   public void rollback(EventServiceConnectedAction action, NoOpResult result, ExecutionContext context) throws ActionException
   {
   }
}
