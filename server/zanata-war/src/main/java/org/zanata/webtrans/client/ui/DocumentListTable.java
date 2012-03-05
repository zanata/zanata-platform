package org.zanata.webtrans.client.ui;

import java.util.Comparator;
import java.util.HashMap;

import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.view.DocumentListView;
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
import com.google.gwt.user.client.History;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

public class DocumentListTable extends CellTable<DocumentNode>
{
   // TODO this is not the ideal place to store this
   // it is required since these graphs have been removed from DocumentNode so
   // that DocumentListPresenter can be unit tested (JRE).
   private HashMap<Long, TransUnitCountGraph> statsWidgets = new HashMap<Long, TransUnitCountGraph>();

   public class TransUnitCountGraphCell extends AbstractCell<TransUnitCountGraph>
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

   private Column<DocumentNode, String> getFolderColumn()
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

   private Column<DocumentNode, String> getDocumentColumn(final org.zanata.webtrans.client.resources.Resources images)
   {
      IconCellDecorator<String> docIconCell = new IconCellDecorator<String>(images.documentImage(), new TextCell());
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

   private Column<DocumentNode, TransUnitCountGraph> getStatisticColumn(final WebTransMessages messages)
   {
      Column<DocumentNode, TransUnitCountGraph> statisticColumn = new Column<DocumentNode, TransUnitCountGraph>(new TransUnitCountGraphCell())
      {
         @Override
         public TransUnitCountGraph getValue(DocumentNode docNode)
         {
            long id = docNode.getDocInfo().getId().getId();
            if (!statsWidgets.containsKey(id))
            {
               TransUnitCountGraph graph = new TransUnitCountGraph(messages);
               graph.setStats(docNode.getDocInfo().getStats());
               statsWidgets.put(id, graph);
            }
            else
            {
               statsWidgets.get(id).setStats(docNode.getDocInfo().getStats());
            }
            return statsWidgets.get(id);
         }
      };
      statisticColumn.setSortable(true);
      return statisticColumn;
   }

   private Column<DocumentNode, String> getTranslatedColumn(final WebTransMessages messages)
   {
      TextColumn<DocumentNode> translatedColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return String.valueOf(object.getDocInfo().getStats().getWordCount().getApproved());
         }
      };
      translatedColumn.setSortable(true);
      return translatedColumn;
   }

   private Column<DocumentNode, String> getUntranslatedColumn(final WebTransMessages messages)
   {
      TextColumn<DocumentNode> unTranslatedColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return String.valueOf(object.getDocInfo().getStats().getWordCount().getUntranslated());

         }
      };
      unTranslatedColumn.setSortable(true);
      return unTranslatedColumn;
   }

   private Column<DocumentNode, String> getRemainingColumn(final WebTransMessages messages)
   {
      TextColumn<DocumentNode> remainingColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return messages.statusBarLabelHours(object.getDocInfo().getStats().getRemainingWordsHours());
         }
      };
      remainingColumn.setSortable(true);
      return remainingColumn;
   }

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

      final Column<DocumentNode, String> folderColumn = getFolderColumn();
      final Column<DocumentNode, String> documentColumn = getDocumentColumn(images);
      final Column<DocumentNode, TransUnitCountGraph> statisticColumn = getStatisticColumn(messages);
      final Column<DocumentNode, String> translatedColumn = getTranslatedColumn(messages);
      final Column<DocumentNode, String> untranslatedColumn = getUntranslatedColumn(messages);
      final Column<DocumentNode, String> remainingColumn = getRemainingColumn(messages);

      addColumn(folderColumn, messages.columnHeaderDirectory());
      addColumn(documentColumn, messages.columnHeaderDocument());
      addColumn(statisticColumn, messages.columnHeaderStatistic());
      addColumn(translatedColumn, messages.columnHeaderTranslated());
      addColumn(untranslatedColumn, messages.columnHeaderUntranslated());
      addColumn(remainingColumn, messages.columnHeaderRemaining());

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
      addColumnStyleName(getColumnIndex(folderColumn), "DocumentListTable_folderCol");
      addColumnStyleName(getColumnIndex(documentColumn), "DocumentListTable_docCol");
      addColumnSortHandler(columnSortHandler);

      getColumnSortList().push(folderColumn);

   }
}
