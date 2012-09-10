package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.TransMemoryResultItem;

public interface HasTMEvent
{

   void showTMDetails(TransMemoryResultItem object);

   void fireCopyEvent(TransMemoryResultItem object);

}
