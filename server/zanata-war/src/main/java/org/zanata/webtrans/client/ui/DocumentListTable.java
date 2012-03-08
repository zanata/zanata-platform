package org.zanata.webtrans.client.ui;

import java.util.Comparator;
import java.util.HashMap;

import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.table.cell.TransUnitCountGraphCell;
import org.zanata.webtrans.client.ui.table.column.DirectoryColumn;
import org.zanata.webtrans.client.ui.table.column.DocumentColumn;
import org.zanata.webtrans.client.ui.table.column.RemainingWordsHoursColumn;
import org.zanata.webtrans.client.ui.table.column.StatisticColumn;
import org.zanata.webtrans.client.ui.table.column.TranslatedColumn;
import org.zanata.webtrans.client.ui.table.column.UntranslatedColumn;
import org.zanata.webtrans.client.view.DocumentListView;
import org.zanata.webtrans.shared.util.ObjectUtil;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class DocumentListTable extends CellTable<DocumentNode>
{
   // TODO this is not the ideal place to store this
   // it is required since these graphs have been removed from DocumentNode so
   // that DocumentListPresenter can be unit tested (JRE).
   private final SingleSelectionModel<DocumentNode> selectionModel = new SingleSelectionModel<DocumentNode>()
   {
      @Override
      public void setSelected(DocumentNode object, boolean selected)
      {
         if (selected && ObjectUtil.equals(object, super.getSelectedObject()))
         {
            // switch to editor (via history) on re-selection
            HistoryToken token = HistoryToken.fromTokenString(History.getToken());
            token.setView(MainView.Editor);
            History.newItem(token.toTokenString());
         }
         super.setSelected(object, selected);
      }
   };

   public DocumentListTable(final DocumentListView documentListView, final org.zanata.webtrans.client.resources.Resources images, final WebTransMessages messages, final ListDataProvider<DocumentNode> dataProvider)
   {
      super();

      selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler()
      {
         public void onSelectionChange(SelectionChangeEvent event)
         {
            DocumentNode selectedNode = selectionModel.getSelectedObject();
            if (selectedNode != null)
            {
               SelectionEvent.fire(documentListView, selectedNode.getDocInfo());
            }
         }
      });

      setStylePrimaryName("DocumentListTable");
      setSelectionModel(selectionModel);

      final Column<DocumentNode, String> directoryColumn = new DirectoryColumn();
      final Column<DocumentNode, String> documentColumn = new DocumentColumn(images);
      final Column<DocumentNode, TransUnitCountGraph> statisticColumn = new StatisticColumn(messages);
      final Column<DocumentNode, String> translatedColumn = new TranslatedColumn();
      final Column<DocumentNode, String> untranslatedColumn = new UntranslatedColumn();
      final Column<DocumentNode, String> remainingColumn = new RemainingWordsHoursColumn(messages);

      directoryColumn.setSortable(true);
      documentColumn.setSortable(true);
      statisticColumn.setSortable(true);
      translatedColumn.setSortable(true);
      untranslatedColumn.setSortable(true);
      remainingColumn.setSortable(true);

      addColumn(directoryColumn, messages.columnHeaderDirectory());
      addColumn(documentColumn, messages.columnHeaderDocument());
      addColumn(statisticColumn, messages.columnHeaderStatistic());
      addColumn(translatedColumn, messages.columnHeaderTranslated());
      addColumn(untranslatedColumn, messages.columnHeaderUntranslated());
      addColumn(remainingColumn, messages.columnHeaderRemaining());

      ListHandler<DocumentNode> columnSortHandler = new ListHandler<DocumentNode>(dataProvider.getList());
      columnSortHandler.setComparator(directoryColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getPath() == null || o2.getDocInfo().getPath() == null)
            {
               return (o1.getDocInfo().getPath() == null) ? -1 : 1;
            }
            else
            {
               return o1.getDocInfo().getPath().compareTo(o2.getDocInfo().getPath());
            }
         }
      });
      columnSortHandler.setComparator(documentColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            return o1.getDocInfo().getName().compareTo(o2.getDocInfo().getName());
         }
      });
      columnSortHandler.setComparator(statisticColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            // StatsByWords is always true for TransUnitCountGraph used in this
            // table
            boolean statsByWords = true;
            if (o1.getDocInfo().getStats().getApprovedPercent(statsByWords) == o2.getDocInfo().getStats().getApprovedPercent(statsByWords))
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getDocInfo().getStats().getApprovedPercent(statsByWords) > o2.getDocInfo().getStats().getApprovedPercent(statsByWords) ? 1 : -1;
            }
            return -1;
         }
      });
      columnSortHandler.setComparator(translatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getStats().getWordCount().getApproved() == o2.getDocInfo().getStats().getWordCount().getApproved())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getDocInfo().getStats().getWordCount().getApproved() > o2.getDocInfo().getStats().getWordCount().getApproved() ? 1 : -1;
            }
            return -1;
         }
      });
      columnSortHandler.setComparator(untranslatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getStats().getWordCount().getUntranslated() == o2.getDocInfo().getStats().getWordCount().getUntranslated())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getDocInfo().getStats().getWordCount().getUntranslated() > o2.getDocInfo().getStats().getWordCount().getUntranslated() ? 1 : -1;
            }
            return -1;
         }
      });
      columnSortHandler.setComparator(remainingColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getStats().getRemainingWordsHours() == o2.getDocInfo().getStats().getRemainingWordsHours())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getDocInfo().getStats().getRemainingWordsHours() > o2.getDocInfo().getStats().getRemainingWordsHours() ? 1 : -1;
            }
            return -1;
         }
      });
      addColumnStyleName(getColumnIndex(directoryColumn), "DocumentListTable_folderCol");
      addColumnStyleName(getColumnIndex(documentColumn), "DocumentListTable_docCol");
      addColumnSortHandler(columnSortHandler);

      getColumnSortList().push(directoryColumn);

   }
}
