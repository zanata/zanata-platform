package org.zanata.webtrans.client.view;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.TransMemoryCopyEvent;
import org.zanata.webtrans.client.presenter.GlossaryPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GlossaryView extends Composite implements GlossaryPresenter.Display
{
   private static GlossaryViewUiBinder uiBinder = GWT.create(GlossaryViewUiBinder.class);

   private static final int CELL_PADDING = 5;
   private static final int HEADER_ROW = 0;
   private static final int SOURCE_COL = 0;
   private static final int SUGGESTION_COL = 1;
   private static final int SIMILARITY_COL = 2;
   private static final int ACTION_COL = 4;

   interface GlossaryViewUiBinder extends UiBinder<Widget, GlossaryView>
   {
   }

   @UiField
   TextBox glossaryTextBox;

   @UiField
   Label glossaryHeader;

   @UiField
   CheckBox exactButton;

   @UiField
   Button searchButton;

   @UiField
   Button clearButton;

   @UiField
   FlexTable resultTable;

   @Inject
   private EventBus eventBus;

   private final Resources resources;

   @Inject
   public GlossaryView(final UiMessages messages, Resources resources)
   {
      this.resources = resources;
      initWidget(uiBinder.createAndBindUi(this));
      exactButton.setText(messages.phraseButtonLabel());
      exactButton.setValue(true);
      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());
      glossaryHeader.setText(messages.glossaryHeader());
      Log.info(LocaleInfo.getCurrentLocale().getLocaleName());
   }

   @UiHandler("glossaryTextBox")
   void onGlossaryTextBoxKeyUp(KeyUpEvent event)
   {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
      {
         searchButton.click();
      }
   }

   @UiHandler("clearButton")
   void onClearButtonClicked(ClickEvent event)
   {
      glossaryTextBox.setText("");
      clearResults();
   }

   @Override
   public HasValue<Boolean> getExactButton()
   {
      return exactButton;
   }

   @Override
   public Button getSearchButton()
   {
      return searchButton;
   }

   public TextBox getGlossaryTextBox()
   {
      return glossaryTextBox;
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   public void clearResults()
   {
      resultTable.removeAllRows();
   }

   @Override
   public void startProcessing()
   {
      clearResults();
      resultTable.setWidget(0, 0, new Label("Loading..."));
   }

   @Override
   public void createTable(ArrayList<TranslationMemoryGlossaryItem> memories)
   {
      // TODO most of this should be in TransMemoryPresenter
      clearResults();
      addColumn("Source", SOURCE_COL);
      addColumn("Suggestion", SUGGESTION_COL);
      addColumn("Similarity", SIMILARITY_COL);

      int row = HEADER_ROW;
      for (final TranslationMemoryGlossaryItem memory : memories)
      {
         ++row;
         final String sourceMessage = memory.getSource();
         final String targetMessage = memory.getTarget();
         final int similarity = memory.getSimilarityPercent();

         resultTable.setWidget(row, SOURCE_COL, new HighlightingLabel(sourceMessage));
         resultTable.setWidget(row, SUGGESTION_COL, new HighlightingLabel(targetMessage));
         resultTable.setText(row, SIMILARITY_COL, similarity + "%");

         final Anchor copyLink = new Anchor("Copy");
         copyLink.addClickHandler(new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               eventBus.fireEvent(new TransMemoryCopyEvent(sourceMessage, targetMessage));
               Log.info("GlossaryCopyEvent event is sent. (" + targetMessage + ")");
            }
         });
         resultTable.setWidget(row, ACTION_COL, copyLink);
         copyLink.setTitle("Copy \"" + targetMessage + "\" to the editor.");
      }
      resultTable.setCellPadding(CELL_PADDING);
   }

   private void addColumn(String columnHeading, int pos)
   {
      Label widget = new Label(columnHeading);
      widget.setWidth("100%");
      widget.addStyleName("TransMemoryTableColumnHeader");
      resultTable.setWidget(HEADER_ROW, pos, widget);
   }
}
