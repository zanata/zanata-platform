package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.TransMemoryResultItem;

public interface TranslationMemoryListener
{

   void showTMDetails(TransMemoryResultItem object);

   void fireCopyEvent(TransMemoryResultItem object);

   void fireSearchEvent();

   void clearContent();

   void onFocus(boolean isFocused);

   void showDiffLegend(boolean show);

   void onTMMergeClick();
}
