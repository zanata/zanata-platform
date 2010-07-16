package org.fedorahosted.flies.webtrans.shared.rpc;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;

public interface HasTransUnitUpdatedData
{

   DocumentId getDocumentId();

   ContentState getNewStatus();

   ContentState getPreviousStatus();

   TransUnitId getTransUnitId();

}
