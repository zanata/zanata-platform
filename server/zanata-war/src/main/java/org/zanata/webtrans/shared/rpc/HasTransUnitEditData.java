package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;

public interface HasTransUnitEditData
{

   DocumentId getDocumentId();

   TransUnitId getTransUnitId();

   String getSessionId();
}
