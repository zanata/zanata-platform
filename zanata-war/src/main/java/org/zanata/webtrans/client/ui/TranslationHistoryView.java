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
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.StackPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   private static final int PAGE_SIZE = 5;
   private static final CellTableResources CELL_TABLE_RESOURCES = GWT.create(CellTableResources.class);
   private final CellTable<TransHistoryItem> historyTable;
   private final EventBus eventBus;
   private final StackPanel container;
   private final VerticalPanel diffPanel;
   private WebTransMessages messages;

   @Inject
   public TranslationHistoryView(WebTransMessages messages, EventBus eventBus)
   {
      super(true, true);
      ensureDebugId("transHistory");
      setGlassEnabled(true);
      getCaption().setText(messages.translationHistoryManagement());

      this.eventBus = eventBus;
      this.messages = messages;

      historyTable = setUpHistoryTable(messages);

      SimplePager simplePager = new SimplePager();
      simplePager.setDisplay(historyTable);

      VerticalPanel historyTableContainer = new VerticalPanel();
      historyTableContainer.add(historyTable);
      historyTableContainer.add(simplePager);

      container = new StackPanel();
      container.add(historyTableContainer, messages.translationHistory());
      diffPanel = new VerticalPanel();
      container.add(diffPanel, messages.comparison());
      setWidget(container);
   }

   private CellTable<TransHistoryItem> setUpHistoryTable(WebTransMessages messages)
   {
      CellTable<TransHistoryItem> historyTable = new CellTable<TransHistoryItem>(PAGE_SIZE, CELL_TABLE_RESOURCES, HISTORY_ITEM_PROVIDES_KEY);
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
   public HasData<TransHistoryItem> getHistoryTable()
   {
      return historyTable;
   }

   @Override
   public void showDiff(List<String> one, List<String> other, String description)
   {
      setComparisonStackTitle(description);
      diffPanel.clear();
      for (int i = 0; i < one.size(); i++)
      {
         String content1 = one.get(i);
         String content2 = other.get(i);
         diffPanel.add(new DiffMatchPatchLabel(content1, content2));
      }
   }

   private void setComparisonStackTitle(String description)
   {
      container.setStackText(1, description);
   }

   @Override
   public void reset()
   {
      historyTable.setPageStart(0);
      diffPanel.clear();
      setComparisonStackTitle(messages.comparison());
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