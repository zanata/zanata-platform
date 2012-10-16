package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class HistoryEntryComparisonPanel extends Composite
{
   private static ComparisonPanelUiBinder ourUiBinder = GWT.create(ComparisonPanelUiBinder.class);
   public static final int ITEM_ONE_ROW = 2;
   public static final int ITEM_TWO_ROW = 3;

   @UiField
   Grid grid;
   @UiField
   WebTransMessages messages;
   @UiField
   SimplePanel itemTwoPanel;
   @UiField
   SimplePanel itemOnePanel;
   @UiField
   PushButton flipButton;
   @UiField
   Styles style;
   private TransHistoryItem itemOne;
   private TransHistoryItem itemTwo;

   public HistoryEntryComparisonPanel()
   {
      initWidget(ourUiBinder.createAndBindUi(this));
   }

   public void compare(TransHistoryItem itemOne, TransHistoryItem itemTwo)
   {
      clear();
      this.itemOne = itemOne;
      this.itemTwo = itemTwo;

      grid.setText(ITEM_ONE_ROW, 0, itemOne.getVersionNum());
      List<String> itemOneContents = itemOne.getContents();
      itemOnePanel.setWidget(new InlineHTML(TranslationDisplay.asSyntaxHighlight(itemOneContents).toSafeHtml()));

      grid.setText(ITEM_TWO_ROW, 0, itemTwo.getVersionNum());
      List<String> itemTwoContents = itemTwo.getContents();
      itemTwoPanel.setWidget(new InlineHTML(TranslationDisplay.asDiff(itemOneContents, itemTwoContents).toSafeHtml()));
   }

   public void clear()
   {
      itemOne = null;
      itemTwo = null;
      grid.setText(ITEM_ONE_ROW, 0, "");
      grid.setText(ITEM_TWO_ROW, 0, "");
      itemOnePanel.clear();
      itemTwoPanel.clear();
   }

   @UiHandler("flipButton")
   public void onFlipButtonClick(ClickEvent event)
   {
      if (itemOne != null && itemTwo != null)
      {
         compare(itemTwo, itemOne);
      }
   }

   interface ComparisonPanelUiBinder extends UiBinder<ScrollPanel, HistoryEntryComparisonPanel>
   {
   }

   interface Styles extends CssResource
   {

      String header();

      String grid();

      String contentCell();

      String originRow();

      String versionCell();

      String flipButton();
   }
}