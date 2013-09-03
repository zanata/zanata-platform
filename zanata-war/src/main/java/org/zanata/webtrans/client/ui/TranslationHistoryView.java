package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.ComparableByDate;
import org.zanata.webtrans.shared.model.ReviewComment;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TranslationHistoryView extends ShortcutContextAwareDialogBox implements TranslationHistoryDisplay
{
   private static TranslationHistoryViewUiBinder uiBinder = GWT.create(TranslationHistoryViewUiBinder.class);
   private final ContentStateRenderer stateRenderer;
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
   UnorderedListWidget itemList;

   @UiField
   ReviewCommentInputWidget commentInput;
   private Listener listener;
   private List<ComparableByDate> items = Lists.newArrayList();

   @Inject
   public TranslationHistoryView(ContentStateRenderer stateRenderer, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(true, true, keyShortcutPresenter);
      this.stateRenderer = stateRenderer;
      closeButton = new DialogBoxCloseButton(this);
      HTMLPanel container = uiBinder.createAndBindUi(this);
      ensureDebugId("transHistory");
      tabLayoutPanel.ensureDebugId("transHistoryTabPanel");
      setGlassEnabled(true);

      setWidget(container);
   }

   @Override
   public void setData(List<ComparableByDate> items)
   {
      this.items = items;
      commentInput.setEnabled(!items.isEmpty());
      redrawList();
   }

   private void redrawList()
   {
      itemList.clear();
      for (ComparableByDate item : items)
      {
         if (item instanceof TransHistoryItem)
         {
            itemList.add(new TransHistoryItemLine((TransHistoryItem) item, listener, stateRenderer));
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
      commentInput.setText("");
   }

   @Override
   public void showDiff(TransHistoryItem one, TransHistoryItem two, String description)
   {
      comparisonPanel.compare(one, two);
      setComparisonTitle(description);
   }

   @Override
   public void disableComparison()
   {
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
      commentInput.setListener(listener);
   }

   private void setComparisonTitle(String description)
   {
      tabLayoutPanel.setTabText(1, description);
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

   }
}
