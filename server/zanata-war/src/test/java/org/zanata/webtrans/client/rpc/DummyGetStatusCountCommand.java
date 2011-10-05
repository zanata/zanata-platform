package org.zanata.webtrans.client.rpc;

import org.zanata.common.ContentState;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.shared.rpc.GetStatusCount;
import org.zanata.webtrans.shared.rpc.GetStatusCountResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DummyGetStatusCountCommand implements Command
{

   private final GetStatusCount action;
   private final AsyncCallback<GetStatusCountResult> callback;

   public DummyGetStatusCountCommand(GetStatusCount action, AsyncCallback<GetStatusCountResult> callback)
   {
      this.action = action;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetStatusCountCommand.execute()");
      int docID = (int) action.getDocumentId().getId();
      TransUnitCount count = new TransUnitCount();
      count.set(ContentState.Approved, 34 * docID);
      count.set(ContentState.NeedReview, 23 * docID);
      count.set(ContentState.New, 43 * docID);
      TransUnitWords words = new TransUnitWords();
      words.set(ContentState.Approved, 70 * docID);
      words.set(ContentState.NeedReview, 40 * docID);
      words.set(ContentState.New, 90 * docID);
      TranslationStats stats = new TranslationStats(count, words);
      callback.onSuccess(new GetStatusCountResult(action.getDocumentId(), stats));
      Log.info("EXIT DummyGetStatusCountCommand.execute()");
   }

}
