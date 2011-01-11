package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnitId;


public interface HasTransUnitUpdatedData
{

   DocumentId getDocumentId();

   ContentState getNewStatus();

   ContentState getPreviousStatus();

   TransUnitId getTransUnitId();

   int getWordCount();
}
