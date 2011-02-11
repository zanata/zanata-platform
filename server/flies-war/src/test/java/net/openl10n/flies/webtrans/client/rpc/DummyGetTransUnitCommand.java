/**
 * 
 */
package net.openl10n.flies.webtrans.client.rpc;

import java.util.ArrayList;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.EditState;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnit;
import net.openl10n.flies.webtrans.shared.model.TransUnitId;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnits;
import net.openl10n.flies.webtrans.shared.rpc.GetTransUnitsResult;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyGetTransUnitCommand implements Command
{
   private final GetTransUnits action;
   private final AsyncCallback<GetTransUnitsResult> callback;

   DummyGetTransUnitCommand(GetTransUnits gtuAction, AsyncCallback<GetTransUnitsResult> callback)
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
      GetTransUnitsResult result = new GetTransUnitsResult(documentId, generateTransUnitSampleData(action.getWorkspaceId().getLocaleId(), count, offset), totalCount);
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
         TransUnit unit = new TransUnit(new TransUnitId(i + 1), localeId, source, sourceComment, target, state);
         units.add(unit);
      }
      return units;
   }

}