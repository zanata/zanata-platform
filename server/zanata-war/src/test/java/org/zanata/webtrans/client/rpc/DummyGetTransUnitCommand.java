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

import com.allen_sauer.gwt.log.client.Log;
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
      Log.info("ENTER DummyGetTransUnitCommand.execute()");
      DocumentId documentId = action.getDocumentId();
      int count = action.getCount();
      int offset = action.getOffset();
      int totalCount = count * 5;
      GetTransUnitListResult result = new GetTransUnitListResult(documentId, generateTransUnitSampleData(action.getWorkspaceId().getLocaleId(), count, offset), totalCount);
      callback.onSuccess(result);
      Log.info("EXIT DummyGetTransUnitCommand.execute()");
   }

   private ArrayList<TransUnit> generateTransUnitSampleData(LocaleId localeId, int numRows, int start)
   {
      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (int i = start; i < start + numRows; i++)
      {
         int stateNum = Random.nextInt(ContentState.values().length);
         ContentState state = ContentState.values()[stateNum];
         String source = "<hellow \nnum=\"" + (i + 1) + "\" />";
         String sourceComment = "comment " + (i + 1);
         String target = "";
         if (state != ContentState.New)
            target = "<world> \"" + (i + 1) + "\"</world>";
         TransUnitId tuid = new TransUnitId(i + 1);
         TransUnit unit = new TransUnit(tuid, tuid.toString(), localeId, source, sourceComment, target, state, "peter", "", "msgContext");
         units.add(unit);
      }
      return units;
   }

}