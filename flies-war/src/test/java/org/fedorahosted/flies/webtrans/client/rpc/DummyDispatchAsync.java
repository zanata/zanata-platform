package org.fedorahosted.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.fedorahosted.flies.webtrans.shared.auth.AuthorizationError;
import org.fedorahosted.flies.webtrans.shared.rpc.AbstractWorkspaceAction;
import org.fedorahosted.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.fedorahosted.flies.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetDocumentList;
import org.fedorahosted.flies.webtrans.shared.rpc.GetDocumentListResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCount;
import org.fedorahosted.flies.webtrans.shared.rpc.GetProjectStatusCountResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetStatusCount;
import org.fedorahosted.flies.webtrans.shared.rpc.GetStatusCountResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransUnits;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTransUnitsResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslationMemory;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslatorList;
import org.fedorahosted.flies.webtrans.shared.rpc.GetTranslatorListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyDispatchAsync extends SeamDispatchAsync
{
   public DummyDispatchAsync()
   {
      Log.info("DummyDispatchAsync()");
   }

   @Override
   public <A extends Action<R>, R extends Result> void execute(A action, AsyncCallback<R> callback)
   {

      if (action instanceof AbstractWorkspaceAction<?>)
      {
         if (this.workspaceContext == null || this.identity == null)
         {
            callback.onFailure(new AuthorizationError("Dispatcher not initialized for WorkspaceActions"));
            return;
         }
         AbstractWorkspaceAction<?> wsAction = (AbstractWorkspaceAction<?>) action;
         wsAction.setWorkspaceId(this.workspaceContext.getWorkspaceId());
         wsAction.setSessionId(this.identity.getSessionId());
      }

      if (action instanceof GetTransUnits)
      {
         GetTransUnits gtuAction = (GetTransUnits) action;
         AsyncCallback<GetTransUnitsResult> gtuCallback = (AsyncCallback<GetTransUnitsResult>) callback;
         DeferredCommand.addCommand(new DummyGetTransUnitCommand(gtuAction, gtuCallback));
      }
      else if (action instanceof GetDocumentList)
      {
         final GetDocumentList gdlAction = (GetDocumentList) action;
         AsyncCallback<GetDocumentListResult> gdlCallback = (AsyncCallback<GetDocumentListResult>) callback;
         DeferredCommand.addCommand(new DummyGetDocsListCommand(gdlAction, gdlCallback));
      }
      else if (action instanceof ActivateWorkspaceAction)
      {
         final ActivateWorkspaceAction gwcAction = (ActivateWorkspaceAction) action;
         AsyncCallback<ActivateWorkspaceResult> gwcCallback = (AsyncCallback<ActivateWorkspaceResult>) callback;
         DeferredCommand.addCommand(new DummyActivateWorkspaceCommand(gwcAction, gwcCallback));
      }
      else if (action instanceof GetTranslatorList)
      {
         final GetTranslatorList _action = (GetTranslatorList) action;
         AsyncCallback<GetTranslatorListResult> _callback = (AsyncCallback<GetTranslatorListResult>) callback;
         DeferredCommand.addCommand(new DummyGetTranslatorListCommand(_action, _callback));
      }
      else if (action instanceof GetProjectStatusCount)
      {
         final GetProjectStatusCount _action = (GetProjectStatusCount) action;
         AsyncCallback<GetProjectStatusCountResult> _callback = (AsyncCallback<GetProjectStatusCountResult>) callback;
         DeferredCommand.addCommand(new DummyGetProjectStatusCountCommand(_action, _callback));
      }
      else if (action instanceof GetStatusCount)
      {
         final GetStatusCount _action = (GetStatusCount) action;
         AsyncCallback<GetStatusCountResult> _callback = (AsyncCallback<GetStatusCountResult>) callback;
         DeferredCommand.addCommand(new DummyGetStatusCountCommand(_action, _callback));
      }
      else if (action instanceof GetTranslationMemory)
      {
         final GetTranslationMemory _action = (GetTranslationMemory) action;
         AsyncCallback<GetTranslationMemoryResult> _callback = (AsyncCallback<GetTranslationMemoryResult>) callback;
         DeferredCommand.addCommand(new DummyGetTranslationMemoryCommand(_action, _callback));
      }
      else
      {
         Log.warn("DummyDispatchAsync: ignoring action of " + action.getClass());
         // callback.onFailure(new RuntimeException());
      }
   }

}
