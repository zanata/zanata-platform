package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   private final ProcessingLabel processingLabel;

   @Inject
   public TranslationHistoryView(ProcessingLabel processingLabel)
   {
      super(true, true);
      this.processingLabel = processingLabel;
      setGlassEnabled(true);
      //TODO localise
      getCaption().setText("Translation History");
      setWidget(processingLabel);
   }

   @Override
   public void center()
   {
      processingLabel.start();
      super.center();
   }

   @Override
   public void hide()
   {
      processingLabel.stop();
      super.hide();
   }

   @Override
   public void showHistory(List<TransHistoryItem> historyItems)
   {
      processingLabel.stop();
      //TODO should use cell table
      Grid grid = new Grid(historyItems.size(), 4);
      for (int i = 0; i < historyItems.size(); i++)
      {
         TransHistoryItem item = historyItems.get(i);
         grid.setWidget(i, 0, new Label(item.getVersionNum()));

         VerticalPanel contentPanel = new VerticalPanel();
         for (String content : item.getContents())
         {
            contentPanel.add(new HighlightingLabel(content));
         }
         grid.setWidget(i, 1, contentPanel);

         grid.getCellFormatter().setStyleName(i, 1, resolveStyle(item.getStatus()));

         grid.setWidget(i, 2, new Label(item.getModifiedBy()));

         grid.setWidget(i, 3, new Label(item.getModifiedDate()));
      }
      setWidget(grid);
   }

   private static String resolveStyle(ContentState status)
   {
      switch (status)
      {
         case NeedReview:
            return "FuzzyStateDecoration";
         case Approved:
            return "ApprovedStateDecoration";
      }
      return "";
   }
}