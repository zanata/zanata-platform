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
      GetTransUnitListResult result = new GetTransUnitListResult(documentId, generateTransUnitSampleData(action.getWorkspaceId().getLocaleId(), count, offset), totalCount, -1);
      callback.onSuccess(result);
      Log.info("EXIT DummyGetTransUnitCommand.execute()");
   }

   private ArrayList<TransUnit> generateTransUnitSampleData(LocaleId localeId, int numRows, int start)
   {
      ArrayList<TransUnit> units = new ArrayList<TransUnit>();
      for (int i = start; i < start + numRows; i++)
      {
         boolean plural = (i % 5 == 0);
         int stateNum = Random.nextInt(ContentState.values().length);
         ContentState state = ContentState.values()[stateNum];
         ArrayList<String> sources = new ArrayList<String>();
         ArrayList<String> targets = new ArrayList<String>();

         sources.add(i % 2 == 0 ? "\n<hellow \nnum=\"" + (i + 1) + "\" /> %s\n" : "\n<hellow \nnum=\"" + (i + 1) + "\" /> %slooooooooooooooooooooooooooooooooooooooonggggggggggggggggggggggggggggggggggggstringgggggggggggggggggggggggggggggggggggggg");
         if (plural)
         {
            sources.add(i % 2 == 0 ? "\n<hellow \nnum=\"" + (i + 2) + "\" /> %s\n" : "\n<hellow \nnum=\"" + (i + 2) + "\" /> %slooooooooooooooooooooooooooooooooooooooonggggggggggggggggggggggggggggggggggggstringgggggggggggggggggggggggggggggggggggggg");
         }
         String sourceComment = "comment " + (i + 1);

         if (state != ContentState.New)
         {
            targets.add("<world> \"" + (i + 1) + "\"</world>");
            if (plural)
            {
               targets.add("<world> \"" + (i + 2) + "\"</world>");
            }
         }

         TransUnitId tuid = new TransUnitId(i + 1);
         TransUnit unit = new TransUnit(tuid, tuid.toString(), localeId, plural, sources, sourceComment, targets, state, "peter", "", "msgContext", i);
         units.add(unit);
      }
      return units;
   }

}