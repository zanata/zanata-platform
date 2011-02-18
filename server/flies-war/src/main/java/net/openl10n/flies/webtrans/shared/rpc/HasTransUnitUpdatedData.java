package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnit;


public interface HasTransUnitUpdatedData
{

   DocumentId getDocumentId();

   ContentState getPreviousStatus();

   int getWordCount();

   TransUnit getTransUnit();

}
