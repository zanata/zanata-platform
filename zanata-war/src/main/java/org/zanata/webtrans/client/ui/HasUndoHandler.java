package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

public interface HasUndoHandler
{
   void preUndo(List<TransUnitUpdateInfo> updateInfoList);
   
   void executeUndo(List<TransUnitUpdateInfo> updateInfoList);
   
   void postSuccess(UpdateTransUnitResult result);
}
