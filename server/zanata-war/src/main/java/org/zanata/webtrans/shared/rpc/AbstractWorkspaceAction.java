package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.WorkspaceId;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;


public abstract class AbstractWorkspaceAction<R extends Result> implements Action<R>
{

   private static final long serialVersionUID = 1L;

   private WorkspaceId workspaceId;
   private SessionId sessionId;

   public final void setWorkspaceId(WorkspaceId workspaceId)
   {
      this.workspaceId = workspaceId;
   }

   public final void setSessionId(SessionId sessionId)
   {
      this.sessionId = sessionId;
   }

   public final WorkspaceId getWorkspaceId()
   {
      return workspaceId;
   }

   public final SessionId getSessionId()
   {
      return sessionId;
   }

}
