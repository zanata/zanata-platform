package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnitId;

public interface HasTransUnitEditData
{

   DocumentId getDocumentId();

   TransUnitId getTransUnitId();

   String getSessionId();
}
