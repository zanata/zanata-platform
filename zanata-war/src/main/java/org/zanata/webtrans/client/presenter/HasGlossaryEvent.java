package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.GlossaryResultItem;

public interface HasGlossaryEvent
{
   void fireCopyEvent(GlossaryResultItem item);

   void showGlossaryDetail(GlossaryResultItem item);

   void fireSearchEvent();
}
