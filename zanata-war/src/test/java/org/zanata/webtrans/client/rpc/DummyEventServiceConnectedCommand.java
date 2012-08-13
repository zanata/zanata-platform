/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;
import org.zanata.webtrans.shared.rpc.NoOpResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyEventServiceConnectedCommand implements Command
{
   private final EventServiceConnectedAction action;
   private final AsyncCallback<NoOpResult> callback;

   DummyEventServiceConnectedCommand(EventServiceConnectedAction gtuAction, AsyncCallback<NoOpResult> callback)
   {
      this.action = gtuAction;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyEventServiceConnectedCommand.execute()");
      callback.onSuccess(new NoOpResult());
      Log.info("EXIT DummyEventServiceConnectedCommand.execute()");
   }
}