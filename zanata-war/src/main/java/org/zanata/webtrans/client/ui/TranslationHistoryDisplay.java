package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.inject.ImplementedBy;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

@ImplementedBy(TranslationHistoryView.class)
public interface TranslationHistoryDisplay extends WidgetDisplay
{
   ProvidesKey<TransHistoryItem> HISTORY_ITEM_PROVIDES_KEY = new ProvidesKey<TransHistoryItem>()
   {
      @Override
      public Object getKey(TransHistoryItem item)
      {
         return item.getVersionNum();
      }
   };

   void center();

   void hide();

   void resetView();

   HasData<TransHistoryItem> getHistoryTable();

   void showDiff(List<String> one, List<String> other, String description);

   void resetComparison();
}
