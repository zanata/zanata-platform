package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.DiffColorLegendPanel;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.client.ui.TextContentsDisplay;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import static org.zanata.webtrans.shared.model.TransMemoryResultItem.MatchType;

public class TransMemoryView extends Composite implements TranslationMemoryDisplay
{

   private static TransMemoryViewUiBinder uiBinder = GWT.create(TransMemoryViewUiBinder.class);
   private List<String> currentQueries;

   interface TransMemoryViewUiBinder extends UiBinder<Widget, TransMemoryView>
   {
   }

   interface Styles extends CssResource
   {
      String headerLabel();

      String rootContainer();

      String searchBox();

      String topBar();
      
      String translated();
      
      String approved();
   }

   @UiField
   Styles style;

   @UiField
   TextBox tmTextBox;

   @UiField
   Label headerLabel;

   @UiField(provided = true)
   ValueListBox<SearchType> searchType;

   @UiField
   Button clearButton, mergeTMButton, searchButton;

   @UiField
   ScrollPanel container;
   @UiField
   RadioButton diffModeDiff;
   @UiField
   RadioButton diffModeHighlight;

   private final FlexTable resultTable;
   private final Label loadingLabel, noResultFoundLabel;
   private final UiMessages messages;
   private final DiffColorLegendPanel diffLegendPanel;

   private TranslationMemoryDisplay.Listener listener;

   private final static int SOURCE_COL = 0;
   private final static int TARGET_COL = 1;
   private final static int NUM_TRANS_COL = 2;
   private final static int ACTION_COL = 3;
   private final static int SIMILARITY_COL = 4;
   private final static int ORIGIN_COL = 5;

   @Inject
   public TransMemoryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, final DiffColorLegendPanel diffLegendPanel)
   {
      this.messages = messages;
      this.diffLegendPanel = diffLegendPanel;

      resultTable = new FlexTable();
      resultTable.setStyleName("resultTable");
      resultTable.setCellSpacing(0);
      resultTable.setCellPadding(3);

      FlexCellFormatter formatter = resultTable.getFlexCellFormatter();

      formatter.setStyleName(0, SOURCE_COL, "th");
      formatter.setStyleName(0, TARGET_COL, "th");
      formatter.setStyleName(0, NUM_TRANS_COL, "th centered numTransCol");
      formatter.setStyleName(0, ACTION_COL, "th centered actionCol");
      formatter.setStyleName(0, SIMILARITY_COL, "th centered similarityCol");
      formatter.setStyleName(0, ORIGIN_COL, "th centered originCol");

      InlineLabel diffLegendInfo = new InlineLabel();
      diffLegendInfo.setStyleName("icon-info-circle-2 details");
      diffLegendInfo.setTitle(messages.colorLegend());

      diffLegendInfo.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.showDiffLegend(true);
         }
      });

      FlowPanel sourceHeader = new FlowPanel();
      sourceHeader.add(new InlineLabel(messages.sourceLabel()));
      sourceHeader.add(diffLegendInfo);

      resultTable.setWidget(0, SOURCE_COL, sourceHeader);
      resultTable.setWidget(0, TARGET_COL, new Label(messages.targetLabel()));

      Label numTrans = new Label(messages.hash());
      numTrans.setTitle(messages.matchCountHeaderTooltip());

      resultTable.setWidget(0, NUM_TRANS_COL, numTrans);
      resultTable.setWidget(0, ACTION_COL, null);
      resultTable.setWidget(0, SIMILARITY_COL, new Label(messages.similarityLabel()));
      resultTable.setWidget(0, ORIGIN_COL, new Label(messages.originLabel()));

      loadingLabel = new Label(messages.searching());
      loadingLabel.setStyleName("tableMsg");
      noResultFoundLabel = new Label(messages.foundNoTMResults());
      noResultFoundLabel.setStyleName("tableMsg");

      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      initWidget(uiBinder.createAndBindUi(this));

      container.setWidget(loadingLabel);

      headerLabel.setText(messages.translationMemoryHeading());
      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());
      mergeTMButton.setText(messages.mergeTMButtonLabel());
      mergeTMButton.setTitle(messages.mergeTMTooltip());

      diffModeDiff.setText(messages.diffModeAsDiff());
      diffModeHighlight.setText(messages.diffModeAsHighlight());
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxKeyPress(KeyPressEvent event)
   {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
      {
         searchButton.click();
      }
   }

   @UiHandler("mergeTMButton")
   public void onMergeButtonClick(ClickEvent event)
   {
      listener.onTMMergeClick();
   }

   @UiHandler("searchButton")
   public void onSearchButtonClick(ClickEvent event)
   {
      listener.fireSearchEvent();
   }

   @Override
   public HasValue<SearchType> getSearchType()
   {
      return searchType;
   }

   @Override
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
      container.setWidget(loadingLabel);

      clearTableContent();
   }

   @Override
   public void stopProcessing(boolean showResult)
   {
      if (!showResult)
      {
         container.setWidget(noResultFoundLabel);
      }
      else
      {
         container.setWidget(resultTable);
      }
   }

   @Override
   public void clearTableContent()
   {
      while (resultTable.getRowCount() > 1)
      {
         resultTable.removeRow(resultTable.getRowCount() - 1);
      }
   }

   @UiHandler({"diffModeDiff", "diffModeHighlight"})
   public void onDiffModeOptionChange(ValueChangeEvent<Boolean> event)
   {
      listener.onDiffModeChanged();
   }

   private DiffMode determineDiffMode()
   {
      return diffModeDiff.getValue() ? DiffMode.NORMAL : DiffMode.HIGHLIGHT;
   }

   private SimplePanel createSourcePanel(TransMemoryResultItem object, List<String> queries)
   {
      SimplePanel panel = new SimplePanel();
      panel.setSize("100%", "100%");
      ArrayList<String> sourceContents = object.getSourceContents();

      // display multiple source strings
      ArrayList<String> queriesPadded = Lists.newArrayList();
      for (int i = 0; i < sourceContents.size(); i++)
      {
         if (queries.size() > i)
         {
            queriesPadded.add(queries.get(i));
         }
         else
         {
            queriesPadded.add(queries.get(0));
         }
      }
      if (determineDiffMode() == DiffMode.NORMAL)
      {
         SafeHtml safeHtml = TextContentsDisplay.asDiff(queriesPadded, sourceContents).toSafeHtml();
         panel.setWidget(new InlineHTML(safeHtml));
      }
      else
      {
         SafeHtml safeHtmlHighlight = TextContentsDisplay.asDiffHighlight(queriesPadded, sourceContents).toSafeHtml();
         panel.setWidget(new InlineHTML(safeHtmlHighlight));
      }
      return panel;
   }

   private SimplePanel createTargetPanel(TransMemoryResultItem object)
   {
      SimplePanel panel = new SimplePanel();
      // display multiple target strings
      
      if(object.getMatchType() == MatchType.ApprovedInternal)
      {
         panel.addStyleName(style.approved());
      }
      else if(object.getMatchType() == MatchType.TranslatedInternal)
      {
         panel.addStyleName(style.translated());
      }
      else if(object.getMatchType() == MatchType.Imported)
      {
         // TODO Add a style
      }
      
      SafeHtml safeHtml = TextContentsDisplay.asSyntaxHighlight(object.getTargetContents()).toSafeHtml();
      panel.setWidget(new InlineHTML(safeHtml));
      return panel;
   }

   @Override
   public void renderTable(List<TransMemoryResultItem> memories, List<String> queries)
   {
      currentQueries = queries;
      for (int i = 0; i < memories.size(); i++)
      {
         final TransMemoryResultItem item = memories.get(i);

         resultTable.setWidget(i + 1, SOURCE_COL, createSourcePanel(item, queries));
         resultTable.setWidget(i + 1, TARGET_COL, createTargetPanel(item));

         Label countLabel = new Label(String.valueOf(item.getMatchCount()));
         countLabel.setTitle(messages.matchCountTooltip(item.getMatchCount()));

         resultTable.setWidget(i + 1, NUM_TRANS_COL, countLabel);
         resultTable.getFlexCellFormatter().setStyleName(i + 1, NUM_TRANS_COL, "centered");

         if (i % 2 == 1)
         {
            resultTable.getRowFormatter().setStyleName(i + 1, "oddRow");
         }

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

         Label similarityLabel = new Label((int) item.getSimilarityPercent() + "%");
         resultTable.setWidget(i + 1, SIMILARITY_COL, similarityLabel);
         resultTable.getFlexCellFormatter().setStyleName(i + 1, SIMILARITY_COL, "centered");

         InlineLabel infoCell = new InlineLabel();
         if( item.getMatchType() == MatchType.Imported )
         {
            String originStr = Joiner.on(", ").join(item.getOrigins());
            String ellipsizedStr = Splitter.fixedLength(10).limit(2).split(originStr).iterator().next() + "...";
            infoCell.setText(ellipsizedStr);
            infoCell.setTitle(originStr);
         }
         else
         {
            infoCell.setStyleName("icon-info-circle-2 details");
            infoCell.addClickHandler(new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               listener.showTMDetails(item);
            }
         });
         }

         resultTable.setWidget(i + 1, ORIGIN_COL, infoCell);
         resultTable.getFlexCellFormatter().setStyleName(i + 1, ORIGIN_COL, "centered");
      }
      container.setWidget(resultTable);
   }

   @Override
   public void redrawTable(List<TransMemoryResultItem> memories)
   {
      for (int i = 0; i < memories.size(); i++)
      {
         TransMemoryResultItem item = memories.get(i);
         resultTable.setWidget(i + 1, SOURCE_COL, createSourcePanel(item, currentQueries));
      }
   }

   @Override
   public void setDisplayMode(DiffMode displayMode)
   {
      if (displayMode == DiffMode.NORMAL)
      {
         diffModeDiff.setValue(true);
      }
      else
      {
         diffModeHighlight.setValue(true);
      }
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxFocus(FocusEvent event)
   {
      listener.onFocus(true);
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxBlur(BlurEvent event)
   {
      listener.onFocus(false);
   }

   @UiHandler("clearButton")
   public void onClearButtonClick(ClickEvent event)
   {
      listener.clearContent();
   }

   @Override
   public void showDiffLegend(boolean show)
   {
      if (show)
      {
         diffLegendPanel.show(ShortcutContext.TM);
      }
      else
      {
         diffLegendPanel.hide(true);
      }

   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }
}
