package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.UpdateType;



public interface HasTransUnitUpdatedData
{

   TransUnitUpdateInfo getUpdateInfo();

   EditorClientId getEditorClientId();

   UpdateType getUpdateType();
}
