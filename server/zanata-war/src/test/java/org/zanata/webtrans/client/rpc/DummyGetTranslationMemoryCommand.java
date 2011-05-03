package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.TranslationMemoryItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;


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
      String query = action.getQuery();
      SearchType type = action.getSearchType();
      ArrayList<TranslationMemoryItem> matches = new ArrayList<TranslationMemoryItem>();
      matches.add(new TranslationMemoryItem("<s>source1</s>", "<tr> &lt;target3</tr>", new Long(3), 85));
      matches.add(new TranslationMemoryItem("<s>source1</s>", "<tr> &lt;target3</tr>", new Long(3), 85));
      matches.add(new TranslationMemoryItem("<s>source1</s>", "<tr> &lt;target3</tr>", new Long(3), 85));
      matches.add(new TranslationMemoryItem("<s>source1</s>", "<tr> &lt;target3</tr>", new Long(3), 85));
      callback.onSuccess(new GetTranslationMemoryResult(matches));
   }

}
