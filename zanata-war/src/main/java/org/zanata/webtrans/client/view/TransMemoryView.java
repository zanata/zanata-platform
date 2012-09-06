package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.DiffMatchPatchLabel;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.client.ui.TooltipTextColumn;
import org.zanata.webtrans.client.ui.Tooltips;
import org.zanata.webtrans.client.ui.table.column.CopyButtonColumn;
import org.zanata.webtrans.client.ui.table.column.DetailsColumn;
import org.zanata.webtrans.client.ui.table.column.SimilarityColumn;
import org.zanata.webtrans.client.ui.table.column.TransMemorySourceColumn;
import org.zanata.webtrans.client.ui.table.column.TransMemoryTargetColumn;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
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

   interface Styles extends CssResource
   {
      String headerLabel();
   }

   @UiField
   Styles style;

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
   Button mergeTMButton;
   
   @UiField
   HTMLPanel container;

//   CellTable<TransMemoryResultItem> tmTable;
   
   private final FlexTable resultTable;
   

   private UiMessages messages;
   private ListDataProvider<TransMemoryResultItem> dataProvider;

   private TransMemorySourceColumn sourceColumn;
   private CopyButtonColumn<TransMemoryResultItem> copyColumn;
   private DetailsColumn<TransMemoryResultItem> detailsColumn;

   private Label noResultWidget = new Label("Found no translation memory results");
   private Label loadingWidget = new Label("Searching translation memory...");
   
   private final Label loadingLabel, noResultFoundLabel;
   
   
   private final static int SOURCE_COL = 0;
   private final static int TARGET_COL = 1;
   private final static int NUM_TRANS_COL = 2;
   private final static int ACTION_COL = 3;
   private final static int SIMILARITY_COL = 4;
   private final static int DETAILS_COL = 5;

   @Inject
   public TransMemoryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, final Resources resources)
   {
      this.messages = messages;
     
      resultTable = new FlexTable();
      resultTable.setStyleName("glossaryTable");
      resultTable.setCellSpacing(0);
      
      FlexCellFormatter formatter = resultTable.getFlexCellFormatter();
      formatter.setStyleName(0, SOURCE_COL, "th");
      formatter.setStyleName(0, TARGET_COL, "th");
      formatter.setStyleName(0, NUM_TRANS_COL, "th");
      formatter.addStyleName(0, NUM_TRANS_COL, "centered");
      formatter.setStyleName(0, ACTION_COL, "th");
      formatter.addStyleName(0, ACTION_COL, "centered");
      formatter.addStyleName(0, ACTION_COL, "actionCol");
      formatter.setStyleName(0, SIMILARITY_COL, "th");
      formatter.addStyleName(0, SIMILARITY_COL, "centered");
      formatter.setStyleName(0, DETAILS_COL, "th");
      formatter.addStyleName(0, DETAILS_COL, "centered");
      formatter.addStyleName(0, DETAILS_COL, "detailCol");

      resultTable.setWidget(0, SOURCE_COL, new Label(messages.sourceLabel()));
      resultTable.setWidget(0, TARGET_COL, new Label(messages.targetLabel()));
      
      Label numTrans = new Label(messages.hash());
      numTrans.setTitle(messages.matchCountHeaderTooltip());
      
      resultTable.setWidget(0, NUM_TRANS_COL, numTrans);
      resultTable.setWidget(0, ACTION_COL, null);
      resultTable.setWidget(0, SIMILARITY_COL, new Label(messages.similarityLabel()));
      resultTable.setWidget(0, DETAILS_COL, new Label(messages.detailsLabel()));
      
      loadingLabel = new Label(messages.searching());
      loadingLabel.setStyleName("tableMsg");
      noResultFoundLabel = new Label(messages.foundNoTMResults());
      noResultFoundLabel.setStyleName("tableMsg");
      
      
//      sourceColumn = new TransMemorySourceColumn();
//      copyColumn = new CopyButtonColumn<TransMemoryResultItem>(messages.copy(), messages.copyTooltip());
//      detailsColumn = new DetailsColumn<TransMemoryResultItem>(resources);

      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      initWidget(uiBinder.createAndBindUi(this));

      headerLabel.setText(messages.translationMemoryHeading());
      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());
      mergeTMButton.setText(messages.mergeTMButtonLabel());
      mergeTMButton.setTitle(messages.mergeTMTooltip());
   }

   @UiHandler("tmTextBox")
   void onTmTextBoxKeyUp(KeyUpEvent event)
   {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
      {
         searchButton.click();
      }
   }

   @Override
   public HasClickHandlers getMergeButton()
   {
      return mergeTMButton;
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
      container.clear();
      container.add(loadingLabel);

      clearTableContent();
   }

   @Override
   public void stopProcessing()
   {
      // TODO hide waiting indicator
      // TODO add test for start and stop processing at appropriate times
   }


   @UiHandler("clearButton")
   public void onClearButtonClicked(ClickEvent event)
   {
      tmTextBox.setText("");
      clearTableContent();
   }
   
   private void clearTableContent()
   {
      for (int i = 1; i < resultTable.getRowCount(); i++)
      {
         resultTable.removeRow(i);
      }
   }
   
   public VerticalPanel getValue(TransMemoryResultItem object, List<String> queries)
   {
      VerticalPanel panel = new VerticalPanel();
      panel.setSize("100%", "100%");
      ArrayList<String> sourceContents = object.getSourceContents();

      // display multiple source/target strings
      for (int i = 0; i < sourceContents.size(); i++)
      {
         String sourceContent = sourceContents.get(i);
         String query;
         if (queries.size() > i)
         {
            query = queries.get(i);
         }
         else
         {
            query = queries.get(0);
         }
         DiffMatchPatchLabel label = new DiffMatchPatchLabel();
         label.setOriginal(query);
         label.setText(sourceContent);
         panel.add(label);
      }
      return panel;
   }
   
   @Override
   public void renderTable(ArrayList<TransMemoryResultItem> memories, List<String> queries)
   {
      startProcessing();
      if(!memories.isEmpty())
      {
         for (int i = 0; i < memories.size(); i++)
         {
            final TransMemoryResultItem item = memories.get(i);

            VerticalPanel soucePanel = getValue(item, queries);
            
            resultTable.setWidget(i + 1, SOURCE_COL, soucePanel);
            resultTable.setWidget(i + 1, TARGET_COL, new HighlightingLabel(item.getTarget()));

            Button copyButton = new Button(messages.copy());
            copyButton.setTitle(messages.copyTooltip());
            
            copyButton.addClickHandler(new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  listener.fireCopyEvent(item);
               }
            });

            resultTable.setWidget(i + 1, ACTION_COL, copyButton);
            resultTable.getFlexCellFormatter().setStyleName(i + 1, ACTION_COL, "centered");
            resultTable.getFlexCellFormatter().addStyleName(i + 1, ACTION_COL, "actionCol");

            InlineLabel infoCell = new InlineLabel();
            infoCell.setStyleName("icon-info-circle-2 details");
            infoCell.addClickHandler(new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  listener.showTMDetail(item);
               }
            });

            resultTable.setWidget(i + 1, DETAILS_COL, infoCell);
            resultTable.getFlexCellFormatter().setStyleName(i + 1, DETAILS_COL, "centered");
            resultTable.getFlexCellFormatter().addStyleName(i + 1, DETAILS_COL, "detailCol");
         }
         container.clear();
         container.add(resultTable);
      }
      else
      {
         container.clear();
         container.add(noResultFoundLabel);
      }
      
      tmTable = new CellTable<TransMemoryResultItem>();
      tmTable.addStyleName("tmTable");
      tmTable.addStyleName("southTable");
      tmTable.addColumn(sourceColumn, messages.sourceLabel());
      tmTable.addColumn(new TransMemoryTargetColumn(), messages.targetLabel());
      TooltipTextColumn<TransMemoryResultItem> countColumn = new TooltipTextColumn<TransMemoryResultItem>()
      {
         @Override
         public String getValue(TransMemoryResultItem item)
         {
            return String.valueOf(item.getMatchCount());
         }

         @Override
         public String getTooltipValue(TransMemoryResultItem item)
         {
            return SafeHtmlUtils.htmlEscape(messages.matchCountTooltip(item.getMatchCount()));
         }
      };

      countColumn.setCellStyleNames(style.narrowCenteredColumn());
      tmTable.addColumn(countColumn, Tooltips.textWithTooltip(messages.matchCountLabel(), messages.matchCountHeaderTooltip()));

      copyColumn.setCellStyleNames(style.narrowCenteredColumn());
      tmTable.addColumn(copyColumn);
      SimilarityColumn<TransMemoryResultItem> similarityColumn = new SimilarityColumn<TransMemoryResultItem>();
      similarityColumn.setCellStyleNames(style.narrowCenteredColumn());
      tmTable.addColumn(similarityColumn, messages.similarityLabel());
      detailsColumn.setCellStyleNames(style.narrowCenteredColumn());
      tmTable.addColumn(detailsColumn, messages.detailsLabel());

   }

   @Override
   public TextBox getFocusTmTextBox()
   {
      return tmTextBox;
   }
}
