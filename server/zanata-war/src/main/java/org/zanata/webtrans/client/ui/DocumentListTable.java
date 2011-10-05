package org.zanata.webtrans.client.ui;

import java.util.Comparator;

import org.zanata.webtrans.client.DocumentListView;
import org.zanata.webtrans.client.DocumentNode;
import org.zanata.webtrans.client.Resources;
import org.zanata.webtrans.client.TransUnitCountGraph;
import org.zanata.webtrans.client.WebTransMessages;
import org.zanata.webtrans.shared.util.ObjectUtil;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public final class DocumentListTable
{
   public static class TransUnitCountGraphCell extends AbstractCell<TransUnitCountGraph>
   {
      public TransUnitCountGraphCell()
      {
         super("mouseover", "mouseout");
      }

      @Override
      public void render(Context arg0, TransUnitCountGraph arg1, SafeHtmlBuilder arg2)
      {
         arg2.appendHtmlConstant(arg1.getElement().getString());
      }

      @Override
      public void onBrowserEvent(Context context, Element parent, TransUnitCountGraph value, NativeEvent event, ValueUpdater<TransUnitCountGraph> valueUpdater)
      {
         if (event.getType().equalsIgnoreCase("mouseover"))
         {
            value.onMouseOver(parent.getFirstChildElement());
         }
         else if (event.getType().equalsIgnoreCase("mouseout"))
         {
            value.onMouseOut();
         }
      }
   }

   private static Column<DocumentNode, String> getFolderColumn(final Resources resources)
   {
      TextColumn<DocumentNode> folderColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return object.getDocInfo().getPath();
         }
      };
      folderColumn.setSortable(true);
      return folderColumn;
   }

   private static Column<DocumentNode, String> getDocumentColumn(final Resources resources)
   {
      IconCellDecorator<String> docIconCell = new IconCellDecorator<String>(resources.documentImage(), new TextCell());
      Column<DocumentNode, String> docColumn = new Column<DocumentNode, String>(docIconCell)
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return object.getDocInfo().getName();
         }
      };
      docColumn.setSortable(true);
      return docColumn;
   }

   private static Column<DocumentNode, TransUnitCountGraph> getStatisticColumn(final Resources resources)
   {
      Column<DocumentNode, TransUnitCountGraph> statisticColumn = new Column<DocumentNode, TransUnitCountGraph>(new TransUnitCountGraphCell())
      {
         @Override
         public TransUnitCountGraph getValue(DocumentNode object)
         {
            return object.getTransUnitCountGraph();
         }
      };
      statisticColumn.setSortable(true);
      return statisticColumn;
   }

   private static Column<DocumentNode, String> getTranslatedColumn(final WebTransMessages messages)
   {
      TextColumn<DocumentNode> translatedColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return String.valueOf(object.getTransUnitCountGraph().getWordsApproved());
         }
      };
      translatedColumn.setSortable(true);
      return translatedColumn;
   }

   private static Column<DocumentNode, String> getUntranslatedColumn(final WebTransMessages messages)
   {
      TextColumn<DocumentNode> unTranslatedColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return String.valueOf(object.getTransUnitCountGraph().getWordsUntranslated());
         }
      };
      unTranslatedColumn.setSortable(true);
      return unTranslatedColumn;
   }

   private static Column<DocumentNode, String> getRemainingColumn(final WebTransMessages messages)
   {
      TextColumn<DocumentNode> remainingColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return messages.statusBarLabelHours(object.getTransUnitCountGraph().getRemainingWordsHours());
         }
      };
      remainingColumn.setSortable(true);
      return remainingColumn;
   }

   public static CellTable<DocumentNode> initDocumentListTable(final DocumentListView documentListView, final Resources resources, final WebTransMessages messages, final ListDataProvider<DocumentNode> dataProvider)
   {

      final SingleSelectionModel<DocumentNode> selectionModel = new SingleSelectionModel<DocumentNode>()
      {
         @Override
         public void setSelected(DocumentNode object, boolean selected)
         {
            if (selected && ObjectUtil.equals(object, super.getSelectedObject()))
            {
               // fire event on re-selection
               SelectionChangeEvent.fire(this);
            }
            super.setSelected(object, selected);
         }
      };
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

      final CellTable<DocumentNode> documentListTable = new CellTable<DocumentNode>();

      documentListTable.setStylePrimaryName("DocumentListTable");
      documentListTable.setSelectionModel(selectionModel);

      final Column<DocumentNode, String> folderColumn = getFolderColumn(resources);
      final Column<DocumentNode, String> documentColumn = getDocumentColumn(resources);
      final Column<DocumentNode, TransUnitCountGraph> statisticColumn = getStatisticColumn(resources);
      final Column<DocumentNode, String> translatedColumn = getTranslatedColumn(messages);
      final Column<DocumentNode, String> untranslatedColumn = getUntranslatedColumn(messages);
      final Column<DocumentNode, String> remainingColumn = getRemainingColumn(messages);

      documentListTable.addColumn(folderColumn, messages.columnHeaderDirectory());
      documentListTable.addColumn(documentColumn, messages.columnHeaderDocument());
      documentListTable.addColumn(statisticColumn, messages.columnHeaderStatistic());
      documentListTable.addColumn(translatedColumn, messages.columnHeaderTranslated());
      documentListTable.addColumn(untranslatedColumn, messages.columnHeaderUntranslated());
      documentListTable.addColumn(remainingColumn, messages.columnHeaderRemaining());

      ListHandler<DocumentNode> columnSortHandler = new ListHandler<DocumentNode>(dataProvider.getList());
      columnSortHandler.setComparator(folderColumn, new Comparator<DocumentNode>()
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
            if (o1.getTransUnitCountGraph().getApprovedPercent() == o2.getTransUnitCountGraph().getApprovedPercent())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getTransUnitCountGraph().getApprovedPercent() > o2.getTransUnitCountGraph().getApprovedPercent() ? 1 : -1;
            }
            return -1;
         }
      });
      columnSortHandler.setComparator(translatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getTransUnitCountGraph().getWordsApproved() == o2.getTransUnitCountGraph().getWordsApproved())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getTransUnitCountGraph().getWordsApproved() > o2.getTransUnitCountGraph().getWordsApproved() ? 1 : -1;
            }
            return -1;
         }
      });
      columnSortHandler.setComparator(untranslatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getTransUnitCountGraph().getWordsUntranslated() == o2.getTransUnitCountGraph().getWordsUntranslated())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getTransUnitCountGraph().getWordsUntranslated() > o2.getTransUnitCountGraph().getWordsUntranslated() ? 1 : -1;
            }
            return -1;
         }
      });
      columnSortHandler.setComparator(remainingColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getTransUnitCountGraph().getRemainingWordsHours() == o2.getTransUnitCountGraph().getRemainingWordsHours())
            {
               return 0;
            }
            if (o1 != null && o2 != null)
            {
               return o1.getTransUnitCountGraph().getRemainingWordsHours() > o2.getTransUnitCountGraph().getRemainingWordsHours() ? 1 : -1;
            }
            return -1;
         }
      });
      documentListTable.addColumnStyleName(documentListTable.getColumnIndex(folderColumn), "DocumentListTable_folderCol");
      documentListTable.addColumnStyleName(documentListTable.getColumnIndex(documentColumn), "DocumentListTable_docCol");
      documentListTable.addColumnSortHandler(columnSortHandler);

      documentListTable.getColumnSortList().push(folderColumn);
      return documentListTable;
   }
}
