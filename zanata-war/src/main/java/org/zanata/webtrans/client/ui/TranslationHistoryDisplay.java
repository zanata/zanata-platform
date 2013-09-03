package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.client.view.ForceReviewCommentDisplay;
import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.inject.ImplementedBy;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

@ImplementedBy(TranslationHistoryView.class)
public interface TranslationHistoryDisplay extends WidgetDisplay
{

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

   interface Listener extends ForceReviewCommentDisplay.Listener
   {

      void copyIntoEditor(List<String> contents);

      void compareClicked(TransHistoryItem item);

      boolean isItemInComparison(TransHistoryItem item);

   }
}
