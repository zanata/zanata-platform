package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.ui.TranslationHistoryDisplay;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransHistoryDataProvider extends ListDataProvider<TransHistoryItem>
{
   public TransHistoryDataProvider()
   {
      super(TranslationHistoryDisplay.HISTORY_ITEM_PROVIDES_KEY);
   }
   
   public void setLoading(boolean loading)
   {
      if (loading)
      {
         updateRowCount(0, false);
      }
      else
      {
         updateRowCount(getList().size(), true);
      }
   }
   
}
