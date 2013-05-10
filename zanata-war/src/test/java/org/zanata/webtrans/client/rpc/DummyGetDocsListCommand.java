/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

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
      ProjectIterationId projectIterationId = action.getWorkspaceId().getProjectIterationId();
      GetDocumentListResult result = new GetDocumentListResult(projectIterationId, generateTransUnitSampleData());
      callback.onSuccess(result);
      Log.info("EXIT DummyGetDocsListCommand.execute()");
   }

   private ArrayList<DocumentInfo> generateTransUnitSampleData()
   {
      HashMap<String, String> extensions = new HashMap<String, String>();
      extensions.put(".po", "");

      ArrayList<DocumentInfo> names = new ArrayList<DocumentInfo>();
      names.add(new DocumentInfo(new DocumentId(new Long(0), ""), "name0", "", LocaleId.EN_US, newStats(0), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(1), ""), "path1name1", "path/1", LocaleId.EN_US, newStats(1), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(2), ""), "path1name2", "path/1", LocaleId.EN_US, newStats(2), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(3), ""), "path2name1", "path/2", LocaleId.EN_US, newStats(3), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(4), ""), "path2name2", "path/2", LocaleId.EN_US, newStats(4), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(5), ""), "name2", "", LocaleId.EN_US, newStats(5), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(6), ""), "name1", "", LocaleId.EN_US, newStats(6), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(7), ""), "long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", "", LocaleId.EN_US, newStats(7), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      names.add(new DocumentInfo(new DocumentId(new Long(8), ""), "another long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", "long path, a path which is, again, really quite long, and also too wide to be displayed without scrolling (in most cases)", LocaleId.EN_US, newStats(8), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      for (int n = 0; n < 100; n++)
      {
         // two digit numbers, to make sorting happier
         names.add(new DocumentInfo(new DocumentId(new Long(n), ""), "multi" + n, "",
 LocaleId.EN_US, newStats(n), new AuditInfo(new Date(), "Translator"), extensions, new AuditInfo(new Date(), "last translator")));
      }
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