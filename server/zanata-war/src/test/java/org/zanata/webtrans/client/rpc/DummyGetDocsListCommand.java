/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.common.ContentState;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

final class DummyGetDocsListCommand implements Command
{
   private final GetDocumentList action;
   private final AsyncCallback<GetDocumentListResult> callback;

   DummyGetDocsListCommand(GetDocumentList gtuAction, AsyncCallback<GetDocumentListResult> callback)
   {
      this.action = gtuAction;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetDocsListCommand.execute()");
      ProjectIterationId projectIterationId = action.getProjectIterationId();
      GetDocumentListResult result = new GetDocumentListResult(projectIterationId, generateTransUnitSampleData());
      // Log.info("CALLBACK DummyGetDocsListCommand.execute()-onSuccess()");
      callback.onSuccess(result);
      Log.info("EXIT DummyGetDocsListCommand.execute()");
   }

   private ArrayList<DocumentInfo> generateTransUnitSampleData()
   {
      ArrayList<DocumentInfo> names = new ArrayList<DocumentInfo>();
      names.add(new DocumentInfo(new DocumentId(0), "name0", "", newStats(0)));
      names.add(new DocumentInfo(new DocumentId(1), "path1name1", "path/1", newStats(1)));
      names.add(new DocumentInfo(new DocumentId(2), "path1name2", "path/1", newStats(2)));
      names.add(new DocumentInfo(new DocumentId(3), "path2name1", "path/2", newStats(3)));
      names.add(new DocumentInfo(new DocumentId(4), "path2name2", "path/2", newStats(4)));
      names.add(new DocumentInfo(new DocumentId(5), "name2", "", newStats(5)));
      names.add(new DocumentInfo(new DocumentId(6), "name1", null, newStats(6)));
      names.add(new DocumentInfo(new DocumentId(7), "long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", null, newStats(7)));
      names.add(new DocumentInfo(new DocumentId(8), "another long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", "long path, a path which is, again, really quite long, and also too wide to be displayed without scrolling (in most cases)", newStats(8)));
      for (int n = 10; n < 129; n++)
         // two digit numbers, to make sorting happier
         names.add(new DocumentInfo(new DocumentId(n), "multi" + n, "", newStats(n)));
      return names;
   }

   private TranslationStats newStats(int docID)
   {
      TransUnitCount count = new TransUnitCount();
      count.set(ContentState.Approved, 34 * docID);
      count.set(ContentState.NeedReview, 23 * docID);
      count.set(ContentState.New, 43 * docID);
      TransUnitWords words = new TransUnitWords();
      words.set(ContentState.Approved, 70 * docID);
      words.set(ContentState.NeedReview, 40 * docID);
      words.set(ContentState.New, 90 * docID);
      TranslationStats stats = new TranslationStats(count, words);
      return stats;
   }

}