package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.view.client.ListDataProvider;
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

   void showDiff(TransHistoryItem one, TransHistoryItem two, String description);

   void disableComparison();

   void setTitle(String title);

   void setListener(Listener listener);

   void setData(List<ComparableByDate> items);

   void addCommentToList(ReviewComment comment);

   void clearInput();

   interface Listener
   {

      void addComment(String commentContent);
   }
}
