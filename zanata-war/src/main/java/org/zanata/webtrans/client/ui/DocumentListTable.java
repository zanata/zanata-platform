/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HasTranslationStats.LabelFormat;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class DocumentListTable extends FlexTable implements HasStatsFilter
{
   private static int PATH_COLUMN = 0;
   private static int DOC_COLUMN = 1;
   private static int STATS_COLUMN = 2;
   private static int TRANSLATED_COLUMN = 3;
   private static int UNTRANSLATED_COLUMN = 4;
   private static int REMAINING_COLUMN = 5;
   private static int LAST_UPLOAD_COLUMN = 6;
   private static int LAST_TRANSLATED_COLUMN = 7;
   private static int ACTION_COLUMN = 8;

   private final UserWorkspaceContext userWorkspaceContext;
   private DocumentListDisplay.Listener listener;
   private final WebTransMessages messages;
   private final Resources resources;

   public DocumentListTable(final UserWorkspaceContext userWorkspaceContext, final WebTransMessages messages, final Resources resources)
   {
      super();
      setStylePrimaryName("DocumentListTable");
      setCellPadding(0);
      setCellSpacing(0);

      this.userWorkspaceContext = userWorkspaceContext;
      
      this.messages = messages;
      this.resources = resources;

      buildHeader();
   }
   
   public void setListener(final DocumentListDisplay.Listener listener)
   {
      this.listener = listener;
   }

   public void clearContent()
   {
      while (this.getRowCount() > 1)
      {
         this.removeRow(this.getRowCount() - 1);
      }
   }

   private class HeaderClickHandler implements ClickHandler
   {
      private String header;
      private boolean asc = true;

      public HeaderClickHandler(String header)
      {
         this.header = header;
      }

      @Override
      public void onClick(ClickEvent event)
      {
         asc = !asc;
         listener.sortList(header, asc);
      }
   }

   private void buildHeader()
   {
      InlineLabel pathHeader = new InlineLabel(messages.columnHeaderPath());
      pathHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.PATH_HEADER));

      InlineLabel docHeader = new InlineLabel(messages.columnHeaderDocument());
      docHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.DOC_HEADER));

      InlineLabel statsHeader = new InlineLabel(messages.columnHeaderStatistic());
      statsHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.STATS_HEADER));

      InlineLabel translatedHeader = new InlineLabel(messages.columnHeaderTranslated());
      translatedHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.TRANSLATED_HEADER));

      InlineLabel untranslatedHeader = new InlineLabel(messages.columnHeaderUntranslated());
      untranslatedHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.UNTRANSLATED_HEADER));

      InlineLabel remainingHeader = new InlineLabel(messages.columnHeaderRemaining());
      remainingHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.REMAINING_HEADER));

      InlineLabel lastUploadHeader = new InlineLabel(messages.columnHeaderLastUpload());
      lastUploadHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.LAST_UPLOAD_HEADER));

      InlineLabel lastTranslatedHeader = new InlineLabel(messages.columnHeaderLastTranslated());
      lastTranslatedHeader.addClickHandler(new HeaderClickHandler(DocumentListDisplay.LAST_TRANSLATED_HEADER));

      InlineLabel actionHeader = new InlineLabel(messages.columnHeaderAction());

      this.setWidget(0, PATH_COLUMN, pathHeader);
      this.setWidget(0, DOC_COLUMN, docHeader);
      this.setWidget(0, STATS_COLUMN, statsHeader);
      this.setWidget(0, TRANSLATED_COLUMN, translatedHeader);
      this.setWidget(0, UNTRANSLATED_COLUMN, untranslatedHeader);
      this.setWidget(0, REMAINING_COLUMN, remainingHeader);
      this.setWidget(0, LAST_UPLOAD_COLUMN, lastUploadHeader);
      this.setWidget(0, LAST_TRANSLATED_COLUMN, lastTranslatedHeader);
      this.setWidget(0, ACTION_COLUMN, actionHeader);

      this.getCellFormatter().setStyleName(0, PATH_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, DOC_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, STATS_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, TRANSLATED_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, UNTRANSLATED_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, REMAINING_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, LAST_UPLOAD_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, LAST_TRANSLATED_COLUMN, "docListHeader sortable");
      this.getCellFormatter().setStyleName(0, ACTION_COLUMN, "docListHeader");
   }

   public HashMap<DocumentId, Integer> buildContent(List<DocumentNode> nodes)
   {
      clearContent();

      HashMap<DocumentId, Integer> pageRows = new HashMap<DocumentId, Integer>();

      for (int i = 0; i < nodes.size(); i++)
      {
         DocumentNode node = nodes.get(i);
         pageRows.put(node.getDocInfo().getId(), i + 1);

         this.setWidget(i + 1, PATH_COLUMN, getPathWidget(node.getDocInfo()));
         this.setWidget(i + 1, DOC_COLUMN, getDocWidget(node.getDocInfo()));

         this.setWidget(i + 1, STATS_COLUMN, getStatsWidget(node.getDocInfo()));
         this.setWidget(i + 1, TRANSLATED_COLUMN, new InlineLabel(String.valueOf(node.getDocInfo().getStats().getWordCount().getApproved())));
         this.setWidget(i + 1, UNTRANSLATED_COLUMN, new InlineLabel(String.valueOf(node.getDocInfo().getStats().getWordCount().getUntranslated())));
         this.setWidget(i + 1, REMAINING_COLUMN, new InlineLabel(messages.statusBarLabelHours(node.getDocInfo().getStats().getRemainingHours())));

         this.setWidget(i + 1, LAST_UPLOAD_COLUMN, new InlineLabel(getAuditInfo(node.getDocInfo().getLastModifiedBy(), node.getDocInfo().getLastChanged())));
         this.setWidget(i + 1, LAST_TRANSLATED_COLUMN, new InlineLabel(getAuditInfo(node.getDocInfo().getLastTranslatedBy(), node.getDocInfo().getLastTranslatedDate())));

         this.setWidget(i + 1, ACTION_COLUMN, getActionWidget(node.getDocInfo()));

         this.getCellFormatter().setStyleName(i + 1, PATH_COLUMN, "pathCol");
         this.getCellFormatter().setStyleName(i + 1, DOC_COLUMN, "documentCol");
         this.getCellFormatter().setStyleName(i + 1, STATS_COLUMN, "statisticCol");
         this.getCellFormatter().setStyleName(i + 1, TRANSLATED_COLUMN, "translatedCol");
         this.getCellFormatter().setStyleName(i + 1, UNTRANSLATED_COLUMN, "untranslatedCol");
         this.getCellFormatter().setStyleName(i + 1, REMAINING_COLUMN, "remainingCol");
         this.getCellFormatter().setStyleName(i + 1, LAST_UPLOAD_COLUMN, "auditCol");
         this.getCellFormatter().setStyleName(i + 1, LAST_TRANSLATED_COLUMN, "auditCol");
         this.getCellFormatter().setStyleName(i + 1, ACTION_COLUMN, "actionCol");
      }

      return pageRows;
   }

   private Widget getPathWidget(final DocumentInfo docInfo)
   {
      InlineLabel pathLabel = new InlineLabel(docInfo.getPath());
      pathLabel.setTitle(docInfo.getPath());

      return pathLabel;
   }

   private Widget getDocWidget(final DocumentInfo docInfo)
   {
      InlineLabel docLabel = new InlineLabel(docInfo.getName());
      docLabel.setTitle(docInfo.getName());
      docLabel.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.fireDocumentSelection(docInfo);
         }
      });
      return docLabel;
   }

   private Widget getStatsWidget(DocumentInfo docInfo)
   {
      final TransUnitCountBar graph = new TransUnitCountBar(messages, LabelFormat.PERCENT_COMPLETE);
      graph.setStats(docInfo.getStats(), true);
     
      return graph;
   }

   private Widget getActionWidget(final DocumentInfo docInfo)
   {
      HorizontalPanel panel = new HorizontalPanel();
      for (Map.Entry<String, String> entry : docInfo.getDownloadExtensions().entrySet())
      {
         Anchor anchor = new Anchor(entry.getKey());
         anchor.setTitle(messages.downloadFileTitle(entry.getKey()));
         anchor.setStyleName("downloadFileLink");
         anchor.setHref(Application.getFileDownloadURL(userWorkspaceContext.getWorkspaceContext().getWorkspaceId(), entry.getValue()));
         anchor.setTarget("_blank");
         panel.add(anchor);
      }
      InlineLabel upload = new InlineLabel();
      upload.setTitle(messages.uploadButtonTitle());
      upload.setStyleName("icon-upload uploadButton");
      upload.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.showUploadDialog(docInfo);
         }
      });
      panel.add(upload);

      return panel;
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

   public void updateRowHasError(int row, boolean hasError)
   {
      if (row > 0)
      {
         if (hasError)
         {
            this.getCellFormatter().setStyleName(row, PATH_COLUMN, "pathCol hasError");
            this.getCellFormatter().setStyleName(row, DOC_COLUMN, "documentCol hasError");
         }
         else
         {
            this.getCellFormatter().setStyleName(row, PATH_COLUMN, "pathCol");
            this.getCellFormatter().setStyleName(row, DOC_COLUMN, "documentCol");
         }
      }

   }

   public void updateLastTranslatedInfo(int row, TransUnit transUnit)
   {
      if (row > 0)
      {
         InlineLabel label = (InlineLabel) this.getWidget(row, LAST_TRANSLATED_COLUMN);
         label.setText(getAuditInfo(transUnit.getLastModifiedBy(), transUnit.getLastModifiedTime()));
      }
   }

   public void updateStats(int row, TranslationStats stats)
   {
      if (row > 0)
      {
         TransUnitCountBar graph = (TransUnitCountBar) this.getWidget(row, STATS_COLUMN);
         graph.setStats(stats, true);
      }
   }

   @Override
   public void setStatsFilter(String option, DocumentNode documentNode)
   {
      for(int i = 0; i < this.getRowCount() - 1; i++)
      {
         TransUnitCountBar graph = (TransUnitCountBar) this.getWidget(i + 1, STATS_COLUMN);
         InlineLabel translated = (InlineLabel) this.getWidget(i + 1, TRANSLATED_COLUMN);
         InlineLabel untranslated = (InlineLabel) this.getWidget(i + 1, UNTRANSLATED_COLUMN);

         if (option.equals(STATS_OPTION_MESSAGE))
         {
            graph.setStatOption(false);
            translated.setText(String.valueOf(documentNode.getDocInfo().getStats().getUnitCount().getApproved()));
            untranslated.setText(String.valueOf(documentNode.getDocInfo().getStats().getUnitCount().getUntranslated()));
         }
         else
         {
            graph.setStatOption(true);
            translated.setText(String.valueOf(documentNode.getDocInfo().getStats().getWordCount().getApproved()));
            untranslated.setText(String.valueOf(documentNode.getDocInfo().getStats().getWordCount().getUntranslated()));
         }
      }
   }
}
