package org.zanata.webtrans.server.rpc;

import java.util.logging.Level;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.RemoteLoggingAction;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("webtrans.gwt.RemoteLoggingHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(RemoteLoggingAction.class)
@Slf4j
public class RemoteLoggingHandler extends AbstractActionHandler<RemoteLoggingAction, NoOpResult>
{
   @In
   ZanataIdentity identity;

   @Override
   public NoOpResult execute(RemoteLoggingAction action, ExecutionContext context) throws ActionException
   {
      try
      {
         identity.checkLoggedIn();
      }
      catch (Exception e)
      {
         log.warn("can not authenticate user.");
      }

      log.error("[gwt-log] from user: {} on workspace: {}", identity.getTrimUsername(), action.getWorkspaceId());
      log.error("[gwt-log] {}", action.getMessage());

      return new NoOpResult();
   }

   @Override
   public void rollback(RemoteLoggingAction action, NoOpResult result, ExecutionContext context) throws ActionException
   {
   }
}
