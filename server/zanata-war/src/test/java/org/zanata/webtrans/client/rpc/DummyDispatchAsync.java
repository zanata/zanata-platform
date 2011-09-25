package org.zanata.webtrans.client.rpc;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.EditingTranslationAction;
import org.zanata.webtrans.shared.rpc.EditingTranslationResult;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;
import org.zanata.webtrans.shared.rpc.GetProjectStatusCount;
import org.zanata.webtrans.shared.rpc.GetProjectStatusCountResult;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
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

      if (action instanceof GetTransUnitList)
      {
         GetTransUnitList gtuAction = (GetTransUnitList) action;
         AsyncCallback<GetTransUnitListResult> gtuCallback = (AsyncCallback<GetTransUnitListResult>) callback;
         DeferredCommand.addCommand(new DummyGetTransUnitCommand(gtuAction, gtuCallback));
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
      else if (action instanceof UpdateTransUnit)
      {
         final UpdateTransUnit _action = (UpdateTransUnit) action;
         AsyncCallback<UpdateTransUnitResult> _callback = (AsyncCallback<UpdateTransUnitResult>) callback;
         DeferredCommand.addCommand(new DummyUpdateTransUnitCommand(_action, _callback));
      }
      else if (action instanceof EditingTranslationAction)
      {
         final EditingTranslationAction _action = (EditingTranslationAction) action;
         AsyncCallback<EditingTranslationResult> _callback = (AsyncCallback<EditingTranslationResult>) callback;
         DeferredCommand.addCommand(new DummyEditingTranslationCommand(_action, _callback));
      }
      else
      {
         Log.warn("DummyDispatchAsync: ignoring action of " + action.getClass());
         // callback.onFailure(new RuntimeException());
      }
   }

}
