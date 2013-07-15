package org.zanata.webtrans.client.ui;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.ContentStateToStyleUtil;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   private static final int COMPARISON_TAB_INDEX = 1;
   private static TranslationHistoryViewUiBinder uiBinder = GWT.create(TranslationHistoryViewUiBinder.class);
   @UiField
   WebTransMessages messages;
   @UiField
   HTMLPanel historyPanel;
   @UiField
   HistoryEntryComparisonPanel comparisonPanel;
   @UiField
   Styles style;
   @UiField(provided = true)
   DialogBoxCloseButton closeButton;
   @UiField
   TabLayoutPanel tabLayoutPanel;

   @UiField
   Button compareButton;
   @UiField
   UnorderedListWidget itemList;
   @UiField
   Button addCommentButton;
   @UiField
   TextArea commentTextArea;
   private Listener listener;
   private List<ComparableByDate> items = Lists.newArrayList();

   public TranslationHistoryView()
   {
      super(true, true);
      closeButton = new DialogBoxCloseButton(this);
      HTMLPanel container = uiBinder.createAndBindUi(this);
      ensureDebugId("transHistory");
      tabLayoutPanel.ensureDebugId("transHistoryTabPanel");
      setGlassEnabled(true);

      commentTextArea.getElement().setAttribute("placeholder", "Add a comment...");

      setWidget(container);
   }

   @Override
   public void setData(List<ComparableByDate> items)
   {
      this.items = items;
      commentTextArea.setEnabled(!items.isEmpty());
      addCommentButton.setEnabled(!items.isEmpty());

      redrawList();
   }

   private void redrawList()
   {
      itemList.clear();
      for (ComparableByDate item : items)
      {
         if (item instanceof TransHistoryItem)
         {
            itemList.add(new TransHistoryItemLine((TransHistoryItem) item, listener));
         }
         if (item instanceof ReviewComment)
         {
            itemList.add(new ReviewCommentItemLine((ReviewComment) item));
         }
      }
   }

   @Override
   public void addCommentToList(ReviewComment comment)
   {
      items.add(0, comment);
      redrawList();
   }

   @Override
   public void clearInput()
   {
      commentTextArea.setValue("");
   }

   @UiHandler("compareButton")
   public void onCompareButtonClick(ClickEvent event)
   {
      tabLayoutPanel.selectTab(COMPARISON_TAB_INDEX);
   }

   @UiHandler("addCommentButton")
   public void onAddCommentButtonClick(ClickEvent event)
   {
      listener.addComment(commentTextArea.getText());
   }

   @Override
   public void showDiff(TransHistoryItem one, TransHistoryItem two, String description)
   {
      compareButton.setEnabled(true);
      comparisonPanel.compare(one, two);
      setComparisonTitle(description);
   }

   @Override
   public void disableComparison()
   {
      compareButton.setEnabled(false);
      comparisonPanel.clear();
      setComparisonTitle(messages.translationHistoryComparisonTitle());
   }

   @Override
   public void setTitle(String title)
   {
      getCaption().setText(title);
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   private void setComparisonTitle(String description)
   {
      tabLayoutPanel.setTabText(1, description);
      compareButton.setText(description);
   }

   @Override
   public void resetView()
   {
      items = Lists.newArrayList();
      redrawList();
      disableComparison();
   }

   interface TranslationHistoryViewUiBinder extends UiBinder<HTMLPanel, TranslationHistoryView>
   {
   }

   interface Styles extends CssResource
   {

      String pasteButton();

      String compareButton();
   }
}
