package org.zanata.webtrans.shared.rpc;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;



public interface HasTransUnitUpdatedData
{

   DocumentId getDocumentId();

   ContentState getPreviousStatus();

   int getWordCount();

   TransUnit getTransUnit();

}
