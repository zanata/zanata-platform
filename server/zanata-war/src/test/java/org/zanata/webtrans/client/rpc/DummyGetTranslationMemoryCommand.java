package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetTranslationMemoryCommand implements Command
{

   private final GetTranslationMemory action;
   private final AsyncCallback<GetTranslationMemoryResult> callback;

   public DummyGetTranslationMemoryCommand(GetTranslationMemory action, AsyncCallback<GetTranslationMemoryResult> callback)
   {
      this.action = action;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetTranslationMemoryCommand.execute()");
      ArrayList<TransMemoryResultItem> matches = new ArrayList<TransMemoryResultItem>();
      ArrayList<String> source = new ArrayList<String>();
      source.add("<s>source1</s>");
      ArrayList<String> target = new ArrayList<String>();
      target.add("<tr> &lt;target3</tr>");
      matches.add(new TransMemoryResultItem(source, target, new Long(3), 85));
      matches.add(new TransMemoryResultItem(source, target, new Long(3), 85));
      matches.add(new TransMemoryResultItem(source, target, new Long(3), 85));
      matches.add(new TransMemoryResultItem(source, target, new Long(3), 85));
      callback.onSuccess(new GetTranslationMemoryResult(action, matches));
      Log.info("EXIT DummyGetTranslationMemoryCommand.execute()");
   }

}
