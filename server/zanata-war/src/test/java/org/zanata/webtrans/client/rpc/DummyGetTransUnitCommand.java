/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyGetTransUnitCommand implements Command
{
   private final GetTransUnitList action;
   private final AsyncCallback<GetTransUnitListResult> callback;

   DummyGetTransUnitCommand(GetTransUnitList gtuAction, AsyncCallback<GetTransUnitListResult> callback)
   {
      this.action = gtuAction;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      DocumentId documentId = action.getDocumentId();
      int count = action.getCount();
      int offset = action.getOffset();
      int totalCount = count * 5;
      GetTransUnitListResult result = new GetTransUnitListResult(documentId, generateTransUnitSampleData(action.getWorkspaceId().getLocaleId(), count, offset), totalCount);
      callback.onSuccess(result);
   }

   private ArrayList<TransUnit> generateTransUnitSampleData(LocaleId localeId, int numRows, int start)
   {
      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (int i = start; i < start + numRows; i++)
      {
         int stateNum = Random.nextInt(ContentState.values().length);
         ContentState state = ContentState.values()[stateNum];
         String source = "<hellow num=\"" + (i + 1) + "\" />";
         String sourceComment = "comment " + (i + 1);
         String target = "";
         if (state != ContentState.New)
            target = "<world> \"" + (i + 1) + "\"</world>";
         TransUnit unit = new TransUnit(new TransUnitId(i + 1), localeId, source, sourceComment, target, state, "peter", "");
         units.add(unit);
      }
      return units;
   }

}