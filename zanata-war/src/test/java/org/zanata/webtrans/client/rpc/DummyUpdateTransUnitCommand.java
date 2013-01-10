/**
 * 
 */
package org.zanata.webtrans.client.rpc;

import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;

final class DummyUpdateTransUnitCommand implements Command
{
   private final UpdateTransUnit action;
   private final AsyncCallback<UpdateTransUnitResult> callback;

   DummyUpdateTransUnitCommand(UpdateTransUnit action, AsyncCallback<UpdateTransUnitResult> callback)
   {
      this.action = action;
      this.callback = callback;
   }

   @Override
   public void execute()
   {
      Log.info("ENTER DummyUpdateTransUnitCommand.execute()");

      TransUnitUpdateRequest firstTu = action.getUpdateRequests().get(0);
      TransUnit tu = TransUnit.Builder.newTransUnitBuilder()
            .setId(firstTu.getTransUnitId().getId())
            .setVerNum(firstTu.getBaseTranslationVersion() + 1)
            .setResId("dummyRestId")
            .setLocaleId(LocaleId.EN_US)
            .setSources(firstTu.getNewContents())
            .build();
      TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(true, true, new DocumentId(0, ""), tu, 5, firstTu.getBaseTranslationVersion(), ContentState.NeedReview);
      UpdateTransUnitResult result = new UpdateTransUnitResult(updateInfo);
      callback.onSuccess(result);
      Log.info("EXIT DummyUpdateTransUnitCommand.execute()");
   }

}