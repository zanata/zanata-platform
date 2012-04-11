package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetGlossaryCommand implements Command
{

   private final GetGlossary action;
   private final AsyncCallback<GetGlossaryResult> callback;

   public DummyGetGlossaryCommand(GetGlossary action, AsyncCallback<GetGlossaryResult> callback)
   {
      this.action = action;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetGlossaryCommand.execute()");
      ArrayList<GlossaryResultItem> matches = new ArrayList<GlossaryResultItem>();
      matches.add(new GlossaryResultItem("<s>source1</s>", "<tr> &lt;suggestion 3</tr>", 3, 100));
      matches.add(new GlossaryResultItem("<s>source1</s>", "<tr> &lt;suggestion 3</tr>", 3, 100));
      matches.add(new GlossaryResultItem("<s>source1</s>", "<tr> &lt;suggestion 3</tr>", 3, 100));
      matches.add(new GlossaryResultItem("<s>source1</s>", "<tr> &lt;suggestion 3</tr>", 3, 100));
      callback.onSuccess(new GetGlossaryResult(action, matches));
      Log.info("EXIT DummyGetGlossaryCommand.execute()");
   }

}
