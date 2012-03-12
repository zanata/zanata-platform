package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.model.HSimpleComment;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetGlossaryDetailsCommand implements Command
{

   private final AsyncCallback<GetGlossaryDetailsResult> callback;

   DummyGetGlossaryDetailsCommand(GetGlossaryDetailsAction action, AsyncCallback<GetGlossaryDetailsResult> callback)
   {
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetGlossaryDetailsCommand.execute()");
      ArrayList<GlossaryDetails> items = new ArrayList<GlossaryDetails>();
      for (int i = 0; i < 4; i++)
      {
         ArrayList<String> srcComments = new ArrayList<String>();
         ArrayList<String> targetComments = new ArrayList<String>();
         
         srcComments.add("Source Comment " + (1 + i));
         srcComments.add("Source Comment " + (2 + i));
         srcComments.add("Source Comment " + (3 + i));
         
         targetComments.add("Target Comment " + (1 + i));
         targetComments.add("Target Comment " + (2 + i));
         targetComments.add("Target Comment " + (3 + i));
         
         items.add(new GlossaryDetails(srcComments, targetComments, "Dummy source ref", "en-us"));
      }
      
      callback.onSuccess(new GetGlossaryDetailsResult(items));
      Log.info("EXIT DummyGetGlossaryDetailsCommand.execute()");

   }
}
