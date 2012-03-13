package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.client.ui.table.column.CopyButtonColumn;
import org.zanata.webtrans.client.ui.table.column.DetailsColumn;
import org.zanata.webtrans.client.ui.table.column.DiffMatchPatchLabelColumn;
import org.zanata.webtrans.client.ui.table.column.HighlightingLabelColumn;
import org.zanata.webtrans.client.ui.table.column.SimilarityColumn;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.HasClickHandlers;
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

public class TransMemoryView extends Composite implements TransMemoryPresenter.Display
{

   private static TransMemoryViewUiBinder uiBinder = GWT.create(TransMemoryViewUiBinder.class);

   interface TransMemoryViewUiBinder extends UiBinder<Widget, TransMemoryView>
   {
   }

   @UiField
   TextBox tmTextBox;

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

   CellTable<TranslationMemoryGlossaryItem> tmTable;

   private boolean isFocused;

   private UiMessages messages;
   private ListDataProvider<TranslationMemoryGlossaryItem> dataProvider;

   private DiffMatchPatchLabelColumn sourceColumn;
   private HighlightingLabelColumn targetColumn;
   private CopyButtonColumn copyColumn;
   private DetailsColumn detailsColumn;

   @Inject
   public TransMemoryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, final Resources resources)
   {
      this.messages = messages;

      sourceColumn = new DiffMatchPatchLabelColumn(true, false);
      targetColumn = new HighlightingLabelColumn(false, true);
      copyColumn = new CopyButtonColumn();
      detailsColumn = new DetailsColumn(resources);

      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      initWidget(uiBinder.createAndBindUi(this));

      headerLabel.setText(messages.translationMemoryHeading());
      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());
   }

   @UiHandler("tmTextBox")
   void onTmTextBoxKeyUp(KeyUpEvent event)
   {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
      {
         searchButton.click();
      }
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxFocus(FocusEvent event)
   {
      isFocused = true;
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxBlur(BlurEvent event)
   {
      isFocused = false;
   }

   @Override
   public HasClickHandlers getClearButton()
   {
      return clearButton;
   }

   @Override
   public HasClickHandlers getSearchButton()
   {
      return searchButton;
   }

   @Override
   public HasValue<SearchType> getSearchType()
   {
      return searchType;
   }

   public TextBox getTmTextBox()
   {
      return tmTextBox;
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void startProcessing()
   {
      // TODO show waiting indicator
   }

   @Override
   public void stopProcessing()
   {
      // TODO hide waiting indicator
      // TODO add test for start and stop processing at appropriate times
   }

   @Override
   public void setPageSize(int size)
   {
      tmTable.setPageSize(size);
   }

   @Override
   public boolean isFocused()
   {
      return isFocused;
   }

   @Override
   public Column<TranslationMemoryGlossaryItem, ImageResource> getDetailsColumn()
   {
      return detailsColumn;
   }

   @Override
   public Column<TranslationMemoryGlossaryItem, String> getCopyColumn()
   {
      return copyColumn;
   }

   @Override
   public void setDataProvider(ListDataProvider<TranslationMemoryGlossaryItem> dataProvider)
   {
      this.dataProvider = dataProvider;
      renderTable();
   }

   private void renderTable()
   {
      tmTable = new CellTable<TranslationMemoryGlossaryItem>();
      tmTable.addStyleName("tmTable");
      tmTable.addStyleName("southTable");
      tmTable.addColumn(sourceColumn, messages.sourceLabel());
      tmTable.addColumn(targetColumn, messages.targetLabel());
      tmTable.addColumn(new SimilarityColumn(), messages.similarityLabel());
      tmTable.addColumn(detailsColumn);
      tmTable.addColumn(copyColumn);

      final NoSelectionModel<TranslationMemoryGlossaryItem> selectionModel = new NoSelectionModel<TranslationMemoryGlossaryItem>();
      final DefaultSelectionEventManager<TranslationMemoryGlossaryItem> manager = DefaultSelectionEventManager.createBlacklistManager(0, 1, 2);
      tmTable.setSelectionModel(selectionModel, manager);

      tmTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

      dataProvider.addDataDisplay(tmTable);

      scrollPanel.clear();
      scrollPanel.add(tmTable);
   }
}
