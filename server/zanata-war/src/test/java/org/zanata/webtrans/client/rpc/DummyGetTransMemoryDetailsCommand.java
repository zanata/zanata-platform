package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetTransMemoryDetailsCommand implements Command
{

   private final AsyncCallback<TransMemoryDetailsList> callback;

   DummyGetTransMemoryDetailsCommand(GetTransMemoryDetailsAction action, AsyncCallback<TransMemoryDetailsList> callback)
   {
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetTransMemoryDetailsCommand.execute()");
      ArrayList<TransMemoryDetails> items = new ArrayList<TransMemoryDetails>();
      for (int i = 0; i < 4; i++)
      {
         items.add(new TransMemoryDetails("source " + i + " comment", "target " + i + " comment", "workspace " + i, "iteration "+ i, "doc id:" + + i));
      }
      
      callback.onSuccess(new TransMemoryDetailsList(items));
      Log.info("EXIT DummyGetTransMemoryDetailsCommand.execute()");

   }
}
