/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyUpdateTransUnitCommand implements Command
{
   private final UpdateTransUnit action;
   private final AsyncCallback<UpdateTransUnitResult> callback;

   DummyUpdateTransUnitCommand(UpdateTransUnit action, AsyncCallback<UpdateTransUnitResult> callback)
   {
      this.action = action;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyUpdateTransUnitCommand.execute()");

      // TransUnitUpdated updated = new TransUnitUpdated(documentId, wordCount,
      // previousStatus, tu);

      UpdateTransUnitResult result = new UpdateTransUnitResult(true);
      callback.onSuccess(result);
      Log.info("EXIT DummyUpdateTransUnitCommand.execute()");
   }

}