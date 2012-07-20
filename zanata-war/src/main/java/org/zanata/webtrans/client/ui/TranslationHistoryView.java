package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   public static final int PAGE_SIZE = 5;
   private final CellTable<TransHistoryItem> historyTable;
   private static CellTableResources cellTableResources = GWT.create(CellTableResources.class);
   private AbstractDataProvider<TransHistoryItem> dataProvider;
   private final EventBus eventBus;

   @Inject
   public TranslationHistoryView(WebTransMessages messages, EventBus eventBus)
   {
      super(true, true);
      setGlassEnabled(true);
      this.eventBus = eventBus;

      historyTable = setUpHistoryTable(messages);

      SimplePager simplePager = new SimplePager();
      simplePager.setDisplay(historyTable);

      VerticalPanel historyTableContainer = new VerticalPanel();
      historyTableContainer.add(historyTable);
      historyTableContainer.add(simplePager);

      StackPanel container = new StackPanel();
      //TODO localise
      container.add(historyTableContainer, "Translation History");
      setWidget(container);
   }

   private CellTable<TransHistoryItem> setUpHistoryTable(WebTransMessages messages)
   {
      CellTable<TransHistoryItem> historyTable = new CellTable<TransHistoryItem>(PAGE_SIZE, cellTableResources, HISTORY_ITEM_PROVIDES_KEY);
      historyTable.setLoadingIndicator(new Label(messages.loading()));
      historyTable.setEmptyTableWidget(new Label(messages.noContent()));

      Column<TransHistoryItem, String> verColumn = createVersionColumn();
      Column<TransHistoryItem, List<String>> contentsColumn = createContentsColumn();
      Column<TransHistoryItem, String> modifiedByColumn = createModifiedByColumn();
      Column<TransHistoryItem, String> modifiedDateColumn = createModifiedDateColumn();
      Column<TransHistoryItem, TransHistoryItem> copyActionColumn = createCopyActionColumn(messages);

      historyTable.addColumn(verColumn, messages.versionNumber());
      historyTable.addColumn(contentsColumn, messages.target());
      historyTable.addColumn(modifiedByColumn, messages.modifiedBy());
      historyTable.addColumn(modifiedDateColumn, messages.modifiedDate());
      historyTable.addColumn(copyActionColumn, messages.actions());

      return historyTable;
   }

   private Column<TransHistoryItem, TransHistoryItem> createCopyActionColumn(WebTransMessages messages)
   {
      Cell<TransHistoryItem> copyActionCell = new ActionCell<TransHistoryItem>(messages.copy(), new ActionCell.Delegate<TransHistoryItem>()
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

   @Override
   public void setDataProvider(AbstractDataProvider<TransHistoryItem> dataProvider)
   {
      this.dataProvider = dataProvider;
      dataProvider.addDataDisplay(historyTable);
   }

   @Override
   public void setHistorySelectionModel(SelectionModel<TransHistoryItem> selectionModel)
   {
      historyTable.setSelectionModel(selectionModel);
   }

   @Override
   public void resetPage()
   {
      historyTable.setPageStart(0);
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

            for (String content : contents)
            {
               HighlightingLabel label = new HighlightingLabel(content);
               appendContent(sb, label.getElement().getString());
            }
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

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div class='translationContainer' style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }
}