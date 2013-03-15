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
import java.util.Date;
import java.util.Map;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.table.column.RemainingHoursColumn;
import org.zanata.webtrans.client.ui.table.column.StaticWidgetColumn;
import org.zanata.webtrans.client.ui.table.column.StatisticColumn;
import org.zanata.webtrans.client.ui.table.column.TooltipColumn;
import org.zanata.webtrans.client.ui.table.column.TranslatedColumn;
import org.zanata.webtrans.client.ui.table.column.UntranslatedColumn;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.IconCellDecorator;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;

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

   private final TooltipColumn<DocumentNode, String> pathColumn;
   private final TooltipColumn<DocumentNode, String> documentColumn;
   private final StatisticColumn statisticColumn;
   private final TranslatedColumn translatedColumn;
   private final UntranslatedColumn untranslatedColumn;
   private final RemainingHoursColumn remainingColumn;

   private final TextColumn<DocumentNode> lastModifiedColumn;
   private final TextColumn<DocumentNode> lastTranslatedColumn;
   private final StaticWidgetColumn<DocumentNode, HorizontalPanel> downloadColumn;
   private final Column<DocumentNode, String> uploadColumn;

   public DocumentListTable(final org.zanata.webtrans.client.resources.Resources images, final WebTransMessages messages, final ListDataProvider<DocumentNode> dataProvider, final DocumentListDisplay.Listener listener, final NoSelectionModel<DocumentNode> selectionModel, final UserWorkspaceContext userWorkspaceContext)
   {
      super(15, (CellTableResources) GWT.create(CellTableResources.class));
      setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
      setStylePrimaryName("DocumentListTable");
      setSelectionModel(selectionModel);

      pathColumn = new TooltipColumn<DocumentNode, String>(new TextCell())
      {
         @Override
         public String getCellStyleNames(Context context, DocumentNode object)
         {
            if (object.getDocInfo().hasValidationError())
            {
               return super.getCellStyleNames(context, object) + " hasError";
            }
            else
            {
               return super.getCellStyleNames(context, object);
            }
         }
         
         @Override
         public String getValue(DocumentNode object)
         {
            return object.getDocInfo().getPath();
         }

         @Override
         public String getTitle(DocumentNode object)
         {
            return object.getDocInfo().getPath();
         }
      };

      documentColumn = new TooltipColumn<DocumentNode, String>(new ClickableTextCell())
      {
         @Override
         public String getCellStyleNames(Context context, DocumentNode object)
         {
            if (object.getDocInfo().hasValidationError())
            {
               return super.getCellStyleNames(context, object) + " hasError";
            }
            else
            {
               return super.getCellStyleNames(context, object);
            }
         }

         @Override
         public String getValue(DocumentNode object)
         {
            return object.getDocInfo().getName();
         }

         @Override
         public String getTitle(DocumentNode object)
         {
            if (object.getDocInfo().hasValidationError())
            {
               return "Contain validation error: " + object.getDocInfo().getName();
            }
            else
            {
               return object.getDocInfo().getName();
            }
         }
      };

      documentColumn.setFieldUpdater(new FieldUpdater<DocumentNode, String>()
      {
         @Override
         public void update(int index, DocumentNode object, String value)
         {
            listener.fireDocumentSelection(object.getDocInfo());
         }
      });

      statisticColumn = new StatisticColumn(messages);
      translatedColumn = new TranslatedColumn();
      untranslatedColumn = new UntranslatedColumn();
      remainingColumn = new RemainingHoursColumn(messages);

      lastModifiedColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return getAuditInfo(object.getDocInfo().getLastModifiedBy(), object.getDocInfo().getLastChanged());
         }
      };

      lastTranslatedColumn = new TextColumn<DocumentNode>()
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return getAuditInfo(object.getDocInfo().getLastTranslatedBy(), object.getDocInfo().getLastTranslatedDate());
         }
      };

      downloadColumn = new StaticWidgetColumn<DocumentNode, HorizontalPanel>()
      {
         @Override
         public HorizontalPanel getValue(DocumentNode object)
         {
            HorizontalPanel downloadPanel = new HorizontalPanel();
            for (Map.Entry<String, String> entry : object.getDocInfo().getDownloadExtensions().entrySet())
            {
               Anchor anchor = new Anchor(entry.getKey());
               anchor.setStyleName("downloadFileLink");
               anchor.setHref(Application.getFileDownloadURL(userWorkspaceContext.getWorkspaceContext().getWorkspaceId(), entry.getValue()));
               anchor.setTarget("_blank");
               downloadPanel.add(anchor);
            }
            return downloadPanel;
         }
      };

      uploadColumn = new Column<DocumentNode, String>(new ButtonCell())
      {
         @Override
         public String getValue(DocumentNode object)
         {
            return "Upload";
         }

      };
      uploadColumn.setFieldUpdater(new FieldUpdater<DocumentNode, String>()
      {
         @Override
         public void update(int index, DocumentNode object, String value)
         {
            listener.showUploadDialog(object.getDocInfo());
         }
      });

      pathColumn.setSortable(true);
      documentColumn.setSortable(true);
      statisticColumn.setSortable(true);
      untranslatedColumn.setSortable(true);
      translatedColumn.setSortable(true);
      remainingColumn.setSortable(true);
      lastModifiedColumn.setSortable(true);
      lastTranslatedColumn.setSortable(true);

      addColumn(pathColumn, messages.columnHeaderPath());
      pathColumn.setCellStyleNames("pathCol");

      DocumentHeader documentColumnHeader = new DocumentHeader(images, messages);
      documentColumn.setCellStyleNames("documentCol");
      addColumn(documentColumn, documentColumnHeader);

      statisticColumn.setCellStyleNames("statisticCol");
      addColumn(statisticColumn, messages.columnHeaderStatistic());

      translatedColumn.setCellStyleNames("translatedCol");
      addColumn(translatedColumn, messages.columnHeaderTranslated());

      untranslatedColumn.setCellStyleNames("untranslatedCol");
      addColumn(untranslatedColumn, messages.columnHeaderUntranslated());

      remainingColumn.setCellStyleNames("remainingCol");
      addColumn(remainingColumn, messages.columnHeaderRemaining());

      lastModifiedColumn.setCellStyleNames("auditCol");
      addColumn(lastModifiedColumn, messages.columnHeaderLastUpload());

      lastTranslatedColumn.setCellStyleNames("auditCol");
      addColumn(lastTranslatedColumn, messages.columnHeaderLastTranslated());

      downloadColumn.setCellStyleNames("downloadCol");
      addColumn(downloadColumn, messages.columnHeaderDownload());

      uploadColumn.setCellStyleNames("uploadCol");
      if (userWorkspaceContext.hasWriteAccess())
      {
         addColumn(uploadColumn, messages.columnHeaderUpload());
      }

      addSorting(dataProvider);
   }

   private String getAuditInfo(String by, Date date)
   {
      StringBuilder sb = new StringBuilder();

      if (date != null)
      {
         sb.append(DateUtil.formatShortDate(date));
      }
      if (!Strings.isNullOrEmpty(by))
      {
         sb.append(" by ");
         sb.append(by);
      }

      return sb.toString();
   }

   public void setStatsFilter(String option)
   {
      statisticColumn.setStatsFilter(option);
      translatedColumn.setStatsFilter(option);
      untranslatedColumn.setStatsFilter(option);
      remainingColumn.setStatsFilter(option);
   }

   private void addSorting(final ListDataProvider<DocumentNode> dataProvider)
   {
      ListHandler<DocumentNode> columnSortHandler = new ListHandler<DocumentNode>(dataProvider.getList());
      columnSortHandler.setComparator(pathColumn, new Comparator<DocumentNode>()
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

      columnSortHandler.setComparator(lastModifiedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getLastChanged() == o2.getDocInfo().getLastChanged())
            {
               return 0;
            }
            if (o1.getDocInfo().getLastChanged() == null)
            {
               return -1;
            }
            if (o2.getDocInfo().getLastChanged() == null)
            {
               return 1;
            }
            return o1.getDocInfo().getLastChanged().after(o2.getDocInfo().getLastChanged()) ? 1 : -1;
         }
      });

      columnSortHandler.setComparator(lastTranslatedColumn, new Comparator<DocumentNode>()
      {
         public int compare(DocumentNode o1, DocumentNode o2)
         {
            if (o1.getDocInfo().getLastChanged() == o2.getDocInfo().getLastChanged())
            {
               return 0;
            }
            if (o1.getDocInfo().getLastChanged() == null)
            {
               return -1;
            }
            if (o2.getDocInfo().getLastChanged() == null)
            {
               return 1;
            }
            return o1.getDocInfo().getLastChanged().after(o2.getDocInfo().getLastChanged()) ? 1 : -1;
         }
      });
      addColumnSortHandler(columnSortHandler);
      getColumnSortList().push(pathColumn);
   }
}
