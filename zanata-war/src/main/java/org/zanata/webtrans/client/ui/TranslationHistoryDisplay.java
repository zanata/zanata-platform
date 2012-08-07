package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
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

   void showDiff(TransHistoryItem one, TransHistoryItem two, String description);

   void disableComparison();

   void addVersionSortHandler(ColumnSortEvent.ListHandler<TransHistoryItem> sortHandler);

   Column<TransHistoryItem, String> getVersionColumn();

   void setSelectionModel(SelectionModel<TransHistoryItem> multiSelectionModel);
}
