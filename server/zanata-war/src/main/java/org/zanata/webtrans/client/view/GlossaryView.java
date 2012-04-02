package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.GlossaryPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.client.ui.table.column.CopyButtonColumn;
import org.zanata.webtrans.client.ui.table.column.DetailsColumn;
import org.zanata.webtrans.client.ui.table.column.HighlightingLabelGlossaryColumn;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class GlossaryView extends Composite implements GlossaryPresenter.Display
{
   private static GlossaryViewUiBinder uiBinder = GWT.create(GlossaryViewUiBinder.class);

   CellTable<GlossaryResultItem> glossaryTable;

   private ListDataProvider<GlossaryResultItem> dataProvider;

   interface GlossaryViewUiBinder extends UiBinder<Widget, GlossaryView>
   {
   }

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

   private final UiMessages messages;
   private boolean isFocused;
   
   private final HighlightingLabelGlossaryColumn sourceColumn;
   private final HighlightingLabelGlossaryColumn targetColumn;
   private final CopyButtonColumn<GlossaryResultItem> copyColumn;
   private final DetailsColumn<GlossaryResultItem> detailsColumn;

   @Inject
   public GlossaryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, Resources resources)
   {
      this.messages = messages;
      
      sourceColumn = new HighlightingLabelGlossaryColumn(true, false);
      targetColumn = new HighlightingLabelGlossaryColumn(false, true);
      copyColumn = new CopyButtonColumn<GlossaryResultItem>();
      detailsColumn = new DetailsColumn<GlossaryResultItem>(resources);
      
      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      dataProvider = new ListDataProvider<GlossaryResultItem>();
      initWidget(uiBinder.createAndBindUi(this));

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

   @UiHandler("glossaryTextBox")
   public void onGlossaryTextBoxFocus(FocusEvent event)
   {
      isFocused = true;
   }

   @UiHandler("glossaryTextBox")
   public void onGlossaryTextBoxBlur(BlurEvent event)
   {
      isFocused = false;
   }

   @UiHandler("clearButton")
   void onClearButtonClicked(ClickEvent event)
   {
      glossaryTextBox.setText("");
      dataProvider.getList().clear();
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
      dataProvider.getList().clear();
   }

   @Override
   public boolean isFocused()
   {
      return isFocused;
   }

   @Override
   public HasValue<SearchType> getSearchType()
   {
      return searchType;
   }

   public void renderTable()
   {
      glossaryTable = new CellTable<GlossaryResultItem>();
      glossaryTable.addStyleName("glossaryTable");
      glossaryTable.addStyleName("southTable");
      glossaryTable.addColumn(sourceColumn, messages.srcTermLabel());
      glossaryTable.addColumn(targetColumn, messages.targetTermLabel());
      glossaryTable.addColumn(detailsColumn, messages.detailsLabel());
      glossaryTable.addColumn(copyColumn);

      final NoSelectionModel<GlossaryResultItem> selectionModel = new NoSelectionModel<GlossaryResultItem>();
      final DefaultSelectionEventManager<GlossaryResultItem> manager = DefaultSelectionEventManager.createBlacklistManager(0, 1, 2);
      glossaryTable.setSelectionModel(selectionModel, manager);

      glossaryTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

      dataProvider.addDataDisplay(glossaryTable);

      scrollPanel.clear();
      scrollPanel.add(glossaryTable);
   }

   @Override
   public Column<GlossaryResultItem, String> getCopyColumn()
   {
      return copyColumn;
   }
   
   @Override
   public Column<GlossaryResultItem, ImageResource> getDetailsColumn()
   {
      return detailsColumn;
   }

   @Override
   public void setDataProvider(ListDataProvider<GlossaryResultItem> dataProvider)
   {
      this.dataProvider = dataProvider;
      renderTable();
   }

   @Override
   public void setPageSize(int size)
   {
      glossaryTable.setPageSize(size);
   }
}
