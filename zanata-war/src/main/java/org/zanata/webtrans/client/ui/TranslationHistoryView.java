package org.zanata.webtrans.client.ui;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.presenter.TransHistoryVersionComparator;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   private static final int PAGE_SIZE = 5;
   private static final int COMPARISON_TAB_INDEX = 1;
   private static final CellTableResources CELL_TABLE_RESOURCES = GWT.create(CellTableResources.class);
   private static TranslationHistoryViewUiBinder uiBinder = GWT.create(TranslationHistoryViewUiBinder.class);
   private final CellTable<TransHistoryItem> historyTable;
   private final EventBus eventBus;
   @UiField
   WebTransMessages messages;
   @UiField
   VerticalPanel historyPanel;
   @UiField
   HistoryEntryComparisonPanel comparisonPanel;
   @UiField
   Styles style;
   @UiField(provided = true)
   DialogBoxCloseButton closeButton;
   @UiField
   TabLayoutPanel tabLayoutPanel;

   private Column<TransHistoryItem,String> versionColumn;
   @UiField
   Button compareButton;

   @Inject
   public TranslationHistoryView(EventBus eventBus)
   {
      super(true, true);
      closeButton = new DialogBoxCloseButton(this);
      HTMLPanel container = uiBinder.createAndBindUi(this);
      this.eventBus = eventBus;
      ensureDebugId("transHistory");
      tabLayoutPanel.ensureDebugId("transHistoryTabPanel");
      setGlassEnabled(true);

      getCaption().setText(messages.translationHistory());

      historyTable = setUpHistoryTable();

      SimplePager simplePager = new SimplePager();
      simplePager.setDisplay(historyTable);

      historyPanel.add(historyTable);
      historyPanel.add(simplePager);
      setWidget(container);
   }

   private CellTable<TransHistoryItem> setUpHistoryTable()
   {
      CellTable<TransHistoryItem> historyTable = new CellTable<TransHistoryItem>(PAGE_SIZE, CELL_TABLE_RESOURCES, HISTORY_ITEM_PROVIDES_KEY);
      historyTable.setEmptyTableWidget(new Label(messages.noContent()));
      historyTable.setLoadingIndicator(new Label(messages.loading()));

      versionColumn = createVersionColumn();
      versionColumn.setSortable(true);
      Column<TransHistoryItem, List<String>> contentsColumn = createContentsColumn();
      Column<TransHistoryItem, String> modifiedByColumn = createModifiedByColumn();
      Column<TransHistoryItem, String> modifiedDateColumn = createModifiedDateColumn();
      Column<TransHistoryItem, TransHistoryItem> pasteActionColumn = createCopyActionColumn(messages);

      historyTable.addColumn(versionColumn, messages.versionNumber());
      historyTable.setColumnWidth(versionColumn, 10, Style.Unit.PCT);
      historyTable.getColumnSortList().push(versionColumn);

      historyTable.addColumn(contentsColumn, messages.target());
      historyTable.setColumnWidth(contentsColumn, 40, Style.Unit.PCT);

      historyTable.addColumn(pasteActionColumn, messages.actions());
      historyTable.setColumnWidth(pasteActionColumn, 20, Style.Unit.PCT);
      pasteActionColumn.setCellStyleNames(style.pasteButton());

      historyTable.addColumn(modifiedByColumn, messages.modifiedBy());
      historyTable.setColumnWidth(modifiedByColumn, 10, Style.Unit.PCT);

      historyTable.addColumn(modifiedDateColumn, messages.modifiedDate());
      historyTable.setColumnWidth(modifiedDateColumn, 20, Style.Unit.PCT);

      return historyTable;
   }

   private static Column<TransHistoryItem, Boolean> createCheckboxColumn(final SelectionModel<TransHistoryItem> selectionModel)
   {
      return new Column<TransHistoryItem, Boolean>(
            new CheckboxCell(true, false)) {
         @Override
         public Boolean getValue(TransHistoryItem object) {
            // Get the value from the selection model.
            return selectionModel.isSelected(object);
         }
      };
   }

   private Column<TransHistoryItem, TransHistoryItem> createCopyActionColumn(WebTransMessages messages)
   {
      Cell<TransHistoryItem> copyActionCell = new ActionCell<TransHistoryItem>(messages.pasteIntoEditor(), new ActionCell.Delegate<TransHistoryItem>()
      {
         @Override
         public void execute(TransHistoryItem historyItem)
         {
            eventBus.fireEvent(new CopyDataToEditorEvent(historyItem.getContents()));
            hide();
         }
      });
      return new Column<TransHistoryItem, TransHistoryItem>(copyActionCell)
      {

         @Override
         public TransHistoryItem getValue(TransHistoryItem object)
         {
            return object;
         }
      };
   }

   @UiHandler("compareButton")
   public void onCompareButtonClick(ClickEvent event)
   {
      tabLayoutPanel.selectTab(COMPARISON_TAB_INDEX);
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
   public void setSelectionModel(SelectionModel<TransHistoryItem> multiSelectionModel)
   {
      historyTable.setSelectionModel(multiSelectionModel, DefaultSelectionEventManager.<TransHistoryItem>createCheckboxManager());
      Column<TransHistoryItem, Boolean> checkboxColumn = createCheckboxColumn(multiSelectionModel);
      historyTable.insertColumn(0, checkboxColumn);
      historyTable.setColumnWidth(checkboxColumn, 10, Style.Unit.PX);
   }

   @Override
   public void setDataProvider(ListDataProvider<TransHistoryItem> dataProvider)
   {
      dataProvider.addDataDisplay(historyTable);
   }

   @Override
   public void addVersionSortHandler(ColumnSortEvent.ListHandler<TransHistoryItem> sortHandler)
   {
      sortHandler.setComparator(versionColumn, TransHistoryVersionComparator.COMPARATOR);
      historyTable.addColumnSortHandler(sortHandler);
      //push it to make column sort in desc order at start
      historyTable.getColumnSortList().push(versionColumn);
   }

   private void setComparisonTitle(String description)
   {
      tabLayoutPanel.setTabText(1, description);
      compareButton.setText(description);
   }

   @Override
   public void resetView()
   {
      historyTable.setPageStart(0);
      disableComparison();
   }

   private static Column<TransHistoryItem, String> createVersionColumn()
   {
      return new Column<TransHistoryItem, String>(new TextCell())
      {
         @Override
         public String getValue(TransHistoryItem historyItem)
         {
            return historyItem.getVersionNum();
         }
      };
   }

   private static Column<TransHistoryItem, List<String>> createContentsColumn()
   {
      Cell<List<String>> contentCell = new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {
            SafeHtml safeHtml = new TranslationWidget().asSyntaxHighlight(contents).toSafeHtml();
            sb.appendHtmlConstant(safeHtml.asString());
         }
      };
      return new Column<TransHistoryItem, List<String>>(contentCell)
      {
         @Override
         public List<String> getValue(TransHistoryItem historyItem)
         {
            return historyItem.getContents();
         }

         @Override
         public String getCellStyleNames(Cell.Context context, TransHistoryItem historyItem)
         {
            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, historyItem));
            if (historyItem.getStatus() == ContentState.Approved)
            {
               styleNames += " ApprovedStateDecoration";
            }
            else if (historyItem.getStatus() == ContentState.NeedReview)
            {
               styleNames += " FuzzyStateDecoration";
            }
            return styleNames;
         }
      };
   }

   private static Column<TransHistoryItem, String> createModifiedByColumn()
   {
      return new Column<TransHistoryItem, String>(new TextCell())
      {
         @Override
         public String getValue(TransHistoryItem historyItem)
         {
            return historyItem.getModifiedBy();
         }
      };
   }

   private static Column<TransHistoryItem, String> createModifiedDateColumn()
   {
      return new Column<TransHistoryItem, String>(new TextCell())
      {
         @Override
         public String getValue(TransHistoryItem historyItem)
         {
            return historyItem.getModifiedDate();
         }
      };
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
