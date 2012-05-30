/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import java.util.Comparator;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.table.column.DirectoryColumn;
import org.zanata.webtrans.client.ui.table.column.DocumentColumn;
import org.zanata.webtrans.client.ui.table.column.RemainingWordsHoursColumn;
import org.zanata.webtrans.client.ui.table.column.StatisticColumn;
import org.zanata.webtrans.client.ui.table.column.TranslatedColumn;
import org.zanata.webtrans.client.ui.table.column.UntranslatedColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

public class DocumentListTable extends CellTable<DocumentNode>
{
   public DocumentListTable(final org.zanata.webtrans.client.resources.Resources images, final WebTransMessages messages, final ListDataProvider<DocumentNode> dataProvider, final SingleSelectionModel<DocumentNode> selectionModel)
   {
      super(15, (CellTableResources) GWT.create(CellTableResources.class));

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
