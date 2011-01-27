package net.openl10n.flies.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import net.openl10n.flies.webtrans.shared.auth.AuthorizationError;
import net.openl10n.flies.webtrans.shared.rpc.AbstractWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceAction;
import net.openl10n.flies.webtrans.shared.rpc.ActivateWorkspaceResult;
import net.openl10n.flies.webtrans.shared.rpc.GetDocumentList;
import net.openl10n.flies.webtrans.shared.rpc.GetDocumentListResult;
import net.openl10n.flies.webtrans.shared.rpc.GetProjectStatusCount;
import net.openl10n.flies.webtrans.shared.rpc.GetProjectStatusCountResult;
import net.openl10n.flies.webtrans.shared.rpc.GetStatusCount;
import net.openl10n.flies.webtrans.shared.rpc.GetStatusCountResult;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnits;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsResult;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslationMemory;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslationMemoryResult;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslatorList;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslatorListResult;

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
