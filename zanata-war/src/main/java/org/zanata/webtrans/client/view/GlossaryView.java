package org.zanata.webtrans.client.view;

import java.util.ArrayList;

import org.zanata.webtrans.client.presenter.GlossaryPresenter;
import org.zanata.webtrans.client.presenter.HasGlossaryEvent;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class GlossaryView extends Composite implements GlossaryPresenter.Display
{
   private static GlossaryViewUiBinder uiBinder = GWT.create(GlossaryViewUiBinder.class);

   interface GlossaryViewUiBinder extends UiBinder<Widget, GlossaryView>
   {
   }

   interface Styles extends CssResource
   {
   }

   @UiField
   Styles style;

   @UiField
   TextBox glossaryTextBox;

   @UiField
   Button searchButton;

   @UiField
   Label headerLabel;

   @UiField(provided = true)
   ValueListBox<SearchType> searchType;

   @UiField
   Button clearButton;

   @UiField
   ScrollPanel scrollPanel;

   private final FlexTable table;

   private final Label loadingLabel, noResultFoundLabel;

   private HasGlossaryEvent listener;

   private final static int SOURCE_COL = 0;
   private final static int TARGET_COL = 1;
   private final static int ACTION_COL = 2;
   private final static int DETAILS_COL = 3;

   @Inject
   public GlossaryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, Resources resources)
   {
      table = new FlexTable();
      table.setStyleName("glossaryTable");
      table.setCellSpacing(0);

      FlexCellFormatter formatter = table.getFlexCellFormatter();
      formatter.setStyleName(0, SOURCE_COL, "th");
      formatter.setStyleName(0, TARGET_COL, "th");
      formatter.setStyleName(0, ACTION_COL, "th");
      formatter.addStyleName(0, ACTION_COL, "centered");
      formatter.addStyleName(0, ACTION_COL, "actionCol");
      formatter.setStyleName(0, DETAILS_COL, "th");
      formatter.addStyleName(0, DETAILS_COL, "centered");
      formatter.addStyleName(0, DETAILS_COL, "detailCol");

      table.setWidget(0, 0, new Label(messages.srcTermLabel()));
      table.setWidget(0, 1, new Label(messages.targetTermLabel()));
      table.setWidget(0, 2, null);
      table.setWidget(0, 3, new Label(messages.detailsLabel()));

      loadingLabel = new Label(messages.loading());
      loadingLabel.setStyleName("tableMsg");
      noResultFoundLabel = new Label(messages.foundNoGlossaryResults());
      noResultFoundLabel.setStyleName("tableMsg");

      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      initWidget(uiBinder.createAndBindUi(this));

      scrollPanel.add(loadingLabel);

      // copyColumn.setCellStyleNames(style.narrowCenteredColumn());
      // detailsColumn.setCellStyleNames(style.narrowCenteredColumn());
      headerLabel.setText(messages.glossaryHeading());
      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());
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
   public void onClearButtonClicked(ClickEvent event)
   {
      glossaryTextBox.setText("");
      clearTableContent();
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

   @Override
   public void startProcessing()
   {
      scrollPanel.clear();
      scrollPanel.add(loadingLabel);

      clearTableContent();
   }

   private void clearTableContent()
   {
      for (int i = 1; i < table.getRowCount(); i++)
      {
         table.removeRow(i);
      }
   }

   @Override
   public HasValue<SearchType> getSearchType()
   {
      return searchType;
   }

   @Override
   public HasAllFocusHandlers getFocusGlossaryTextBox()
   {
      return glossaryTextBox;
   }

   @Override
   public void renderTable(ArrayList<GlossaryResultItem> glossaries)
   {
      startProcessing();

      if (!glossaries.isEmpty())
      {
         for (int i = 0; i < glossaries.size(); i++)
         {
            final GlossaryResultItem item = glossaries.get(i);

            table.setWidget(i + 1, SOURCE_COL, new HighlightingLabel(item.getSource()));
            table.setWidget(i + 1, TARGET_COL, new HighlightingLabel(item.getTarget()));

            Button copyButton = new Button("Copy");
            copyButton.addClickHandler(new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  listener.fireCopyEvent(item);
               }
            });

            table.setWidget(i + 1, ACTION_COL, copyButton);
            table.getFlexCellFormatter().setStyleName(i + 1, ACTION_COL, "centered");
            table.getFlexCellFormatter().addStyleName(i + 1, ACTION_COL, "actionCol");

            InlineLabel infoCell = new InlineLabel();
            infoCell.setStyleName("icon-info-circle-2 details");
            infoCell.addClickHandler(new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  listener.showGlossaryDetail(item);
               }
            });

            table.setWidget(i + 1, DETAILS_COL, infoCell);
            table.getFlexCellFormatter().setStyleName(i + 1, DETAILS_COL, "centered");
            table.getFlexCellFormatter().addStyleName(i + 1, DETAILS_COL, "detailCol");
         }
         scrollPanel.clear();
         scrollPanel.add(table);
      }
      else
      {
         scrollPanel.add(noResultFoundLabel);
      }
   }

   @Override
   public void setListener(HasGlossaryEvent listener)
   {
      this.listener = listener;

   }
}
