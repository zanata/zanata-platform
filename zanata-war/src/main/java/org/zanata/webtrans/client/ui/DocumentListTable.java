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
import org.zanata.webtrans.client.ui.table.column.RemainingHoursColumn;
import org.zanata.webtrans.client.ui.table.column.StatisticColumn;
import org.zanata.webtrans.client.ui.table.column.TranslatedColumn;
import org.zanata.webtrans.client.ui.table.column.UntranslatedColumn;

import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

public class DocumentListTable extends CellTable<DocumentNode>
{
   private final class DocumentHeader extends Header<String>
   {
      private final WebTransMessages messages;
      public DocumentHeader(org.zanata.webtrans.client.resources.Resources images, WebTransMessages messages)
      {
         super(new IconCellDecorator<String>(images.documentImage(), new TextCell()));
         this.messages = messages;
      }

      @Override
      public String getValue()
      {
         return messages.columnHeaderDocument();
      }
   }
   
   private final class StatisticHeader extends Header<String> implements HasStatsFilter
   {
      private final WebTransMessages messages;
      private String statsOption = STATS_OPTION_WORDS;

      public StatisticHeader(WebTransMessages messages)
      {
         super(new TextCell());
         this.messages = messages;
      }
      @Override
      public String getValue()
      {
         return messages.columnHeaderStatistic(statsOption);
      }

      @Override
      public void setStatsFilter(String option)
      {
         statsOption = option;
      }
   }

   private final class TranslatedHeader extends Header<String> implements HasStatsFilter
   {
      private final WebTransMessages messages;
      private String statsOption = STATS_OPTION_WORDS;

      public TranslatedHeader(WebTransMessages messages)
      {
         super(new TextCell());
         this.messages = messages;
      }

      @Override
      public String getValue()
      {
         return messages.columnHeaderTranslated(statsOption);
      }

      @Override
      public void setStatsFilter(String option)
      {
         statsOption = option;
      }
   }

   private final class UntranslatedHeader extends Header<String> implements HasStatsFilter
   {
      private final WebTransMessages messages;
      private String statsOption = STATS_OPTION_WORDS;

      public UntranslatedHeader(WebTransMessages messages)
      {
         super(new TextCell());
         this.messages = messages;
      }

      @Override
      public String getValue()
      {
         return messages.columnHeaderUntranslated(statsOption);
      }

      @Override
      public void setStatsFilter(String option)
      {
         statsOption = option;
      }
   }

   private final TextColumn<DocumentNode> directoryColumn;
   private final TextColumn<DocumentNode> documentColumn;
   private final StatisticColumn statisticColumn;
   private final TranslatedColumn translatedColumn;
   private final UntranslatedColumn untranslatedColumn;
   private final RemainingHoursColumn remainingColumn;

   private final TranslatedHeader translatedColumnHeader;
   private final UntranslatedHeader untranslatedColumnHeader;
   private final StatisticHeader statisticColumnHeader;

   public DocumentListTable(final org.zanata.webtrans.client.resources.Resources images, final WebTransMessages messages, final ListDataProvider<DocumentNode> dataProvider, final SingleSelectionModel<DocumentNode> selectionModel)
   {
      super(15, (CellTableResources) GWT.create(CellTableResources.class));

      setStylePrimaryName("DocumentListTable");
      setSelectionModel(selectionModel);

      directoryColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return object.getDocInfo().getPath();
         }
      };

      documentColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return object.getDocInfo().getName();
         }
      };

      statisticColumn = new StatisticColumn(messages);
      translatedColumn = new TranslatedColumn();
      untranslatedColumn = new UntranslatedColumn();
      remainingColumn = new RemainingHoursColumn(messages);

      directoryColumn.setSortable(true);
      documentColumn.setSortable(true);
      statisticColumn.setSortable(true);
      translatedColumn.setSortable(true);
      untranslatedColumn.setSortable(true);
      remainingColumn.setSortable(true);
      
      
      addColumn(directoryColumn, messages.columnHeaderDirectory());
      directoryColumn.setCellStyleNames("directoryCol");

      DocumentHeader documentColumnHeader = new DocumentHeader(images, messages);
      documentColumn.setCellStyleNames("documentCol");
      addColumn(documentColumn, documentColumnHeader);

      statisticColumnHeader = new StatisticHeader(messages);
      statisticColumn.setCellStyleNames("statisticCol");
      addColumn(statisticColumn, statisticColumnHeader);

      translatedColumnHeader = new TranslatedHeader(messages);
      translatedColumn.setCellStyleNames("translatedCol");
      addColumn(translatedColumn, translatedColumnHeader);

      untranslatedColumnHeader = new UntranslatedHeader(messages);
      untranslatedColumn.setCellStyleNames("untranslatedCol");
      addColumn(untranslatedColumn, untranslatedColumnHeader);

      remainingColumn.setCellStyleNames("remainingCol");
      addColumn(remainingColumn, messages.columnHeaderRemaining());

      addSorting(dataProvider);
   }

   public void setStatsFilter(String option)
   {
      translatedColumnHeader.setStatsFilter(option);
      untranslatedColumnHeader.setStatsFilter(option);
      statisticColumnHeader.setStatsFilter(option);

      statisticColumn.setStatsFilter(option);
      translatedColumn.setStatsFilter(option);
      untranslatedColumn.setStatsFilter(option);
      remainingColumn.setStatsFilter(option);
   }

   private void addSorting(final ListDataProvider<DocumentNode> dataProvider)
   {
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
            return o1.getDocInfo().getStats().getApprovedPercent(statsByWords) - o2.getDocInfo().getStats().getApprovedPercent(statsByWords);
         }
      });
      columnSortHandler.setComparator(translatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            return o1.getDocInfo().getStats().getWordCount().getApproved() - o2.getDocInfo().getStats().getWordCount().getApproved();
         }
      });
      columnSortHandler.setComparator(untranslatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            return o1.getDocInfo().getStats().getWordCount().getUntranslated() - o2.getDocInfo().getStats().getWordCount().getUntranslated();
         }
      });
      columnSortHandler.setComparator(remainingColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getStats().getRemainingHours() == o2.getDocInfo().getStats().getRemainingHours())
            {
               return 0;
            }
            return o1.getDocInfo().getStats().getRemainingHours() > o2.getDocInfo().getStats().getRemainingHours() ? 1 : -1;
         }
      });
      addColumnSortHandler(columnSortHandler);
      getColumnSortList().push(directoryColumn);
   }
}
