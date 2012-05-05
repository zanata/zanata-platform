package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;



public interface HasTransUnitUpdatedData
{

   TransUnitUpdateInfo getUpdateInfo();

   SessionId getSessionId();

}
