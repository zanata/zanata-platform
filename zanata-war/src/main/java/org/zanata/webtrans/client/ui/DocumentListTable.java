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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zanata.rest.dto.stats.CommonContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HasTranslationStats.LabelFormat;
import org.zanata.webtrans.client.util.DateUtil;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class DocumentListTable extends FlexTable
{
   public static enum DocValidationStatus
   {
      HasError, NoError, Unknown
   }

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

   public HashMap<DocumentId, Integer> buildContent(List<DocumentNode> nodes, boolean statsByWords)
   {
      clearContent();

      HashMap<DocumentId, Integer> pageRows = new HashMap<DocumentId, Integer>();

      for (int i = 0; i < nodes.size(); i++)
      {
         DocumentNode node = nodes.get(i);
         pageRows.put(node.getDocInfo().getId(), i + 1);

         this.setWidget(i + 1, PATH_COLUMN, getPathWidget(node.getDocInfo()));
         this.setWidget(i + 1, DOC_COLUMN, new DocWidget(node.getDocInfo()));

         this.setWidget(i + 1, STATS_COLUMN, getStatsWidget(node.getDocInfo(), statsByWords));
         this.setWidget(i + 1, TRANSLATED_COLUMN, getTranslatedWidget(node.getDocInfo(), statsByWords));
         this.setWidget(i + 1, UNTRANSLATED_COLUMN, getUntranslatedWidget(node.getDocInfo(), statsByWords));
         this.setWidget(i + 1, REMAINING_COLUMN, getRemainingWidget(node.getDocInfo()));

         this.setWidget(i + 1, LAST_UPLOAD_COLUMN, new InlineLabel(getAuditInfo(node.getDocInfo().getLastModified())));
         this.setWidget(i + 1, LAST_TRANSLATED_COLUMN, new InlineLabel(getAuditInfo(node.getDocInfo().getLastTranslated())));

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
         
         if (node.getDocInfo().hasError() == null)
         {
            updateRowHasError(i + 1, DocValidationStatus.Unknown);
         }
         else if (node.getDocInfo().hasError())
         {
            updateRowHasError(i + 1, DocValidationStatus.HasError);
         }
         else if (!node.getDocInfo().hasError())
         {
            updateRowHasError(i + 1, DocValidationStatus.NoError);
         }
      }

      return pageRows;
   }

   private Widget getPathWidget(final DocumentInfo docInfo)
   {
      InlineLabel pathLabel = new InlineLabel(docInfo.getPath());
      pathLabel.setTitle(docInfo.getPath());

      return pathLabel;
   }

   private interface HasValidationResult
   {
      void showLoading();

      void setValidationResult(DocValidationStatus status);
   }

   private class DocWidget extends FlowPanel implements HasValidationResult
   {
      private final InlineLabel docLabel;
      private final Image loading;
      private final InlineLabel noError;
      private final InlineLabel hasError;

      public DocWidget(final DocumentInfo docInfo)
      {
         super();
         loading = new Image(resources.spinner());
         loading.setVisible(false);
         this.add(loading);

         noError = new InlineLabel();
         noError.setStyleName("icon-ok-circle-2");
         noError.setVisible(false);
         this.add(noError);

         hasError = new InlineLabel();
         hasError.setStyleName("icon-cancel-circle-2 hasError");
         hasError.setVisible(false);
         this.add(hasError);

         docLabel = new InlineLabel(docInfo.getName());
         docLabel.setTitle(docInfo.getName());
         docLabel.addClickHandler(new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               listener.fireDocumentSelection(docInfo);
            }
         });
         this.add(docLabel);
      }

      @Override
      public void showLoading()
      {
         loading.setVisible(true);

         noError.setVisible(false);
         hasError.setVisible(false);
      }

      @Override
      public void setValidationResult(DocValidationStatus status)
      {
         loading.setVisible(false);
         noError.setVisible(false);
         hasError.setVisible(false);

         if (status == DocValidationStatus.HasError)
         {
            hasError.setVisible(true);
            docLabel.setTitle(messages.hasValidationErrors(docLabel.getText()));
            docLabel.addStyleName("hasError");
         }
         else if (status == DocValidationStatus.NoError)
         {
            noError.setVisible(true);
            docLabel.setTitle(docLabel.getText());
            docLabel.removeStyleName("hasError");
         }
         else if (status == DocValidationStatus.Unknown)
         {
            docLabel.setTitle(docLabel.getText());
            docLabel.removeStyleName("hasError");
         }
      }
   }

   private Widget getStatsWidget(DocumentInfo docInfo, boolean statsByWords)
   {
      FlowPanel panel = new FlowPanel();
      final TransUnitCountBar graph = new TransUnitCountBar(userWorkspaceContext, messages, LabelFormat.PERCENT_COMPLETE, false, userWorkspaceContext.getWorkspaceRestrictions().isProjectRequireReview());
      Image loading = new Image(resources.spinner());
      panel.add(graph);
      panel.add(loading);

      if (docInfo.getStats() == null)
      {
         loading.setVisible(true);
         graph.setVisible(false);
      }
      else
      {
         loading.setVisible(false);
         graph.setVisible(true);
         graph.setStats(docInfo.getStats(), statsByWords);
      }

      return panel;
   }

   private Widget getTranslatedWidget(DocumentInfo docInfo, boolean statsByWords)
   {
      String text = "0";
      if (docInfo.getStats() != null)
      {
         String locale = userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId().getId();
         if (statsByWords)
         {
            text = String.valueOf(docInfo.getStats().getStats(locale, StatUnit.WORD).getApproved());
         }
         else
         {
            text = String.valueOf(docInfo.getStats().getStats(locale, StatUnit.MESSAGE).getApproved());
         }
      }
      return new InlineLabel(text);
   }

   private Widget getUntranslatedWidget(DocumentInfo docInfo, boolean statsByWords)
   {
      String text = "0";
      if (docInfo.getStats() != null)
      {
         String locale = userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId().getId();
         if (statsByWords)
         {
            text = String.valueOf(docInfo.getStats().getStats(locale, StatUnit.WORD).getUntranslated());
         }
         else
         {
            text = String.valueOf(docInfo.getStats().getStats(locale, StatUnit.MESSAGE).getUntranslated());
         }
      }
      return new InlineLabel(text);
   }

   private Widget getRemainingWidget(DocumentInfo docInfo)
   {
      String text = "0";
      if (docInfo.getStats() != null)
      {
         text = messages.statusBarLabelHours(docInfo.getStats().getStats(userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId().getId(), StatUnit.WORD).getRemainingHours());
      }
      return new InlineLabel(text);
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

   private String getAuditInfo(AuditInfo lastTranslatedInfo)
   {
      StringBuilder sb = new StringBuilder();

      if (lastTranslatedInfo != null)
      {
         if (lastTranslatedInfo.getDate() != null)
         {
            sb.append(DateUtil.formatShortDate(lastTranslatedInfo.getDate()));
         }
         if (!Strings.isNullOrEmpty(lastTranslatedInfo.getUsername()))
         {
            sb.append(" by ");
            sb.append(lastTranslatedInfo.getUsername());
         }
      }
      return sb.toString();
   }

   public void updateRowHasError(int row, DocValidationStatus status)
   {
      HasValidationResult panel = (HasValidationResult) this.getWidget(row, DOC_COLUMN);
      panel.setValidationResult(status);
   }

   public void updateLastTranslatedInfo(int row, AuditInfo lastTranslated)
   {
      HasText label = (HasText) this.getWidget(row, LAST_TRANSLATED_COLUMN);
      label.setText(getAuditInfo(lastTranslated));
   }

   public void updateStats(int row, CommonContainerTranslationStatistics stats, boolean statsByWords)
   {
      if (stats != null)
      {
         FlowPanel panel = (FlowPanel) this.getWidget(row, STATS_COLUMN);

         TransUnitCountBar graph = (TransUnitCountBar) panel.getWidget(0);
         graph.setStats(stats, true);
         graph.setVisible(true);

         Image loading = (Image) panel.getWidget(1);
         loading.setVisible(false);

         HasText translated = (HasText) this.getWidget(row, TRANSLATED_COLUMN);
         HasText untranslated = (HasText) this.getWidget(row, UNTRANSLATED_COLUMN);

         graph.setStatOption(statsByWords);

         String locale = userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId().toString();
         if (statsByWords)
         {
            translated.setText(String.valueOf(stats.getStats(locale, StatUnit.WORD).getApproved()));
            untranslated.setText(String.valueOf(stats.getStats(locale, StatUnit.WORD).getUntranslated()));
         }
         else
         {
            translated.setText(String.valueOf(stats.getStats(locale, StatUnit.MESSAGE).getApproved()));
            untranslated.setText(String.valueOf(stats.getStats(locale, StatUnit.MESSAGE).getUntranslated()));
         }
      }
   }

   public void setStatsFilter(boolean statsByWords, Integer row)
   {
      FlowPanel panel = (FlowPanel) this.getWidget(row, STATS_COLUMN);

      TransUnitCountBar graph = (TransUnitCountBar) panel.getWidget(0);
      graph.setStatOption(statsByWords);

      HasText translated = (HasText) this.getWidget(row, TRANSLATED_COLUMN);
      HasText untranslated = (HasText) this.getWidget(row, UNTRANSLATED_COLUMN);

      if (statsByWords)
      {
         translated.setText(String.valueOf(graph.getWordsApproved()));
         untranslated.setText(String.valueOf(graph.getWordsUntranslated()));
      }
      else
      {
         translated.setText(String.valueOf(graph.getUnitApproved()));
         untranslated.setText(String.valueOf(graph.getUnitUntranslated()));
      }
   }

   public void showRowLoading(int row)
   {
      HasValidationResult panel = (HasValidationResult) this.getWidget(row, DOC_COLUMN);
      panel.showLoading();
   }
}
