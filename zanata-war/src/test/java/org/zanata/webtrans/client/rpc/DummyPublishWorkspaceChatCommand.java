package org.zanata.webtrans.client.rpc;

import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyPublishWorkspaceChatCommand implements Command
{

   private final AsyncCallback<PublishWorkspaceChatResult> callback;
   private final PublishWorkspaceChatAction action;

   DummyPublishWorkspaceChatCommand(PublishWorkspaceChatAction action, AsyncCallback<PublishWorkspaceChatResult> callback)
   {
      this.callback = callback;
      this.action = action;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyPublishWorkspaceChatCommand.execute()");
      
      callback.onSuccess(new PublishWorkspaceChatResult());
      Log.info("EXIT PublishWorkspaceChatResult.execute()");

   }
}
