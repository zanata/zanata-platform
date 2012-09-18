/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import java.util.ArrayList;
import java.util.HashMap;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyGetTransUnitsNavigationCommand implements Command
{
   private final GetTransUnitsNavigation action;
   private final AsyncCallback<GetTransUnitsNavigationResult> callback;

   DummyGetTransUnitsNavigationCommand(GetTransUnitsNavigation gtunAction, AsyncCallback<GetTransUnitsNavigationResult> callback)
   {
      this.action = gtunAction;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyGetTransUnitsNavigationCommand.execute()");
      
      ArrayList<Long> idIndexList = new ArrayList<Long>();
      HashMap<Long, ContentState> transIdStateList = new HashMap<Long, ContentState>();
      
      
      GetTransUnitsNavigationResult result = new GetTransUnitsNavigationResult(new DocumentId(action.getId()), idIndexList, transIdStateList);
      callback.onSuccess(result);
      Log.info("EXIT DummyGetTransUnitCommand.execute()");
   }

}