/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;


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
      ProjectIterationId projectIterationId = action.getProjectIterationId();
      GetDocumentListResult result = new GetDocumentListResult(projectIterationId, generateTransUnitSampleData());
      callback.onSuccess(result);
   }

   private ArrayList<DocumentInfo> generateTransUnitSampleData()
   {
      ArrayList<DocumentInfo> names = new ArrayList<DocumentInfo>();
      names.add(new DocumentInfo(new DocumentId(1), "path1name1", "path/1"));
      names.add(new DocumentInfo(new DocumentId(2), "path1name2", "path/1"));
      names.add(new DocumentInfo(new DocumentId(3), "path2name1", "path/2"));
      names.add(new DocumentInfo(new DocumentId(4), "path2name2", "path/2"));
      names.add(new DocumentInfo(new DocumentId(5), "name2", ""));
      names.add(new DocumentInfo(new DocumentId(6), "name1", null));
      names.add(new DocumentInfo(new DocumentId(7), "long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", null));
      names.add(new DocumentInfo(new DocumentId(8), "another long name, a name which is really quite long, too wide to be displayed without scrolling (in most cases)", "long path, a path which is, again, really quite long, and also too wide to be displayed without scrolling (in most cases)"));
      for (int n = 10; n < 99; n++)
         // two digit numbers, to make sorting happier
         names.add(new DocumentInfo(new DocumentId(n), "multi" + n, ""));
      return names;
   }

}