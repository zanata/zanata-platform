package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.rpc.NoOpResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyPublishWorkspaceChatCommand implements Command
{

   private final AsyncCallback<NoOpResult> callback;

   DummyPublishWorkspaceChatCommand(AsyncCallback<NoOpResult> callback)
   {
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyPublishWorkspaceChatCommand.execute()");
      
      callback.onSuccess(new NoOpResult());
      Log.info("EXIT PublishWorkspaceChatResult.execute()");

   }
}
