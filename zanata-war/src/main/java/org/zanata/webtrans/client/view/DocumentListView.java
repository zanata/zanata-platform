/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.client.view;

import java.util.HashMap;
import java.util.List;

import org.zanata.common.CommonContainerTranslationStatistics;
import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DocumentListTable;
import org.zanata.webtrans.client.ui.DocumentListTable.DocValidationStatus;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.DownloadFilesConfirmationBox;
import org.zanata.webtrans.client.ui.FileUploadDialog;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.ui.LoadingPanel;
import org.zanata.webtrans.client.ui.Pager;
import org.zanata.webtrans.client.ui.SearchField;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DocumentListView extends Composite implements DocumentListDisplay
{
   interface DocumentListViewUiBinder extends UiBinder<LayoutPanel, DocumentListView>
   {
   }

   private static DocumentListViewUiBinder uiBinder = GWT.create(DocumentListViewUiBinder.class);

   private DocumentListDisplay.Listener listener;

   @UiField(provided = true)
   SearchField searchField;

   @UiField
   FlowPanel documentListContainer;

   @UiField
   CheckBox exactSearchCheckBox, caseSensitiveCheckBox;

   @UiField
   RadioButton statsByMsg, statsByWord;

   @UiField
   PushButton downloadAllFiles;

   @UiField(provided = true)
   Pager pager;

   private DocumentListTable documentListTable;

   private final DownloadFilesConfirmationBox confirmationBox;
   private final FileUploadDialog fileUploadDialog;

   private final LoadingPanel loadingPanel;

   private Timer timer = new Timer()
   {
      public void run()
      {
         getDownloadStatus();
      }
   };

   @Inject
   public DocumentListView(Resources resources, WebTransMessages messages, UserWorkspaceContext userworkspaceContext, LoadingPanel loadingPanel)
   {
      this.loadingPanel = loadingPanel;

      confirmationBox = new DownloadFilesConfirmationBox(false, messages, resources);
      fileUploadDialog = new FileUploadDialog(resources);
      pager = new Pager(messages, resources);
      searchField = new SearchField(this);
      searchField.setTextBoxTitle(messages.docListFilterDescription());

      initWidget(uiBinder.createAndBindUi(this));

      caseSensitiveCheckBox.setTitle(messages.docListFilterCaseSensitiveDescription());
      exactSearchCheckBox.setTitle(messages.docListFilterExactMatchDescription());
      statsByMsg.setText(messages.byMessage());
      statsByWord.setText(messages.byWords());

      documentListTable = new DocumentListTable(userworkspaceContext, messages,resources);
      documentListContainer.add(documentListTable);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public String getSelectedStatsOption()
   {
      if (statsByMsg.getValue())
      {
         return STATS_OPTION_MESSAGE;
      }
      else
      {
         return STATS_OPTION_WORDS;
      }
   }

   @UiHandler("exactSearchCheckBox")
   public void onExactSearchCheckboxChange(ValueChangeEvent<Boolean> event)
   {
      listener.fireExactSearchToken(event.getValue());
   }

   @UiHandler("caseSensitiveCheckBox")
   public void onCaseSensitiveCheckboxValueChange(ValueChangeEvent<Boolean> event)
   {
      listener.fireCaseSensitiveToken(event.getValue());
   }

   @UiHandler("statsByMsg")
   public void onStatsByMsgChange(ValueChangeEvent<Boolean> event)
   {
      if (event.getValue())
      {
         listener.statsOptionChange();
      }
   }

   @UiHandler("statsByWord")
   public void onStatsByWordChange(ValueChangeEvent<Boolean> event)
   {
      if (event.getValue())
      {
         listener.statsOptionChange();
      }
   }

   @UiHandler("downloadAllFiles")
   public void onDownloadAllFilesClick(ClickEvent event)
   {
      confirmationBox.center();
   }
   
   @UiHandler("pager")
   public void onPagerValueChanged(ValueChangeEvent<Integer> event)
   {
      listener.pagerValueChanged(event.getValue());
   }

   @Override
   public void setLayout(String layout)
   {
      documentListContainer.setStyleName(layout);
   }

   @Override
   public void updateFilter(boolean docFilterCaseSensitive, boolean docFilterExact, String docFilterText)
   {
      caseSensitiveCheckBox.setValue(docFilterCaseSensitive, false);
      exactSearchCheckBox.setValue(docFilterExact, false);
      searchField.setText(docFilterText);
   }

   @Override
   public void setListener(Listener documentListPresenter)
   {
      this.listener = documentListPresenter;
      confirmationBox.registerHandler(listener);
      fileUploadDialog.registerHandler(listener, Application.getUploadFileUrl());
      documentListTable.setListener(listener);
   }

   @Override
   public void onSearchFieldValueChange(String value)
   {
      listener.fireFilterToken(value);
   }

   @Override
   public void onSearchFieldCancel()
   {
      searchField.setValue("");
   }

   @Override
   public void onSearchFieldBlur()
   {
   }

   @Override
   public void onSearchFieldFocus()
   {
   }

   @Override
   public void onSearchFieldClick()
   {
   }

   @Override
   public void hideConfirmation()
   {
      confirmationBox.hide();
   }

   @Override
   public void updateFileDownloadProgress(int currentProgress, int maxProgress)
   {
      confirmationBox.setProgressMessage(currentProgress + " of " + maxProgress);
   }

   @Override
   public void setDownloadInProgress(boolean inProgress)
   {
      confirmationBox.setInProgress(inProgress);
   }

   @Override
   public void setAndShowFilesDownloadLink(String url)
   {
      confirmationBox.setDownloadLink(url);
      confirmationBox.showDownloadLink(true);
   }

   @Override
   public InlineLink getDownloadAllFilesInlineLink(final String url)
   {
      return new InlineLink()
      {
         @Override
         public Widget asWidget()
         {
            Anchor anchor = new Anchor();
            anchor.setStyleName("icon-download");
            anchor.addStyleName("downloadLink");
            anchor.setHref(url);
            anchor.setTarget("_blank");
            return anchor;
         }

         @Override
         public void setLinkStyle(String styleName)
         {

         }

         @Override
         public void setDisabledStyle(String styleName)
         {

         }
      };
   }

   @Override
   public void showUploadDialog(DocumentInfo info, WorkspaceId workspaceId)
   {
      fileUploadDialog.setDocumentInfo(info, workspaceId);
      fileUploadDialog.center();
   }

   @Override
   public void closeFileUpload()
   {
      fileUploadDialog.hide();
   }

   @Override
   public String getSelectedUploadFileName()
   {
      return fileUploadDialog.getUploadFileName();
   }

   @Override
   public void submitUploadForm()
   {
      fileUploadDialog.submitForm();
   }

   @Override
   public void startGetDownloadStatus(int periodMillis)
   {
      timer.scheduleRepeating(periodMillis);
   }

   public void getDownloadStatus()
   {
      listener.updateDownloadFileProgress();
   }

   @Override
   public void stopGetDownloadStatus()
   {
      timer.cancel();
   }

   @Override
   public void setEnableDownloadZip(boolean enabled)
   {
      downloadAllFiles.setEnabled(enabled);

   }

   @Override
   public void setDownloadZipButtonText(String text)
   {
      downloadAllFiles.setText(text);
   }

   @Override
   public void setDownloadZipButtonTitle(String title)
   {
      downloadAllFiles.setTitle(title);
   }

   @Override
   public void showLoading(boolean showLoading)
   {
      if (showLoading)
      {
         loadingPanel.center();
      }
      else
      {
         loadingPanel.hide();
      }
   }

   @Override
   public HashMap<DocumentId, Integer> buildContent(List<DocumentNode> nodes)
   {
      return documentListTable.buildContent(nodes, statsByWord.getValue().booleanValue());
   }

   @Override
   public void updateRowHasError(int row, DocValidationStatus status)
   {
      documentListTable.updateRowHasError(row, status);
   }

   @Override
   public void updateLastTranslated(int row, AuditInfo lastTranslated)
   {
      documentListTable.updateLastTranslatedInfo(row, lastTranslated);

   }

   @Override
   public void updateStats(int row, CommonContainerTranslationStatistics stats)
   {
      documentListTable.updateStats(row, stats, statsByWord.getValue().booleanValue());
   }

   @Override
   public void setStatsFilters(Integer row)
   {
      documentListTable.setStatsFilter(statsByWord.getValue().booleanValue(), row);
   }
   
   @Override
   public void setStatsFilters(String option)
   {
      if (option.equals(STATS_OPTION_MESSAGE))
      {
         statsByMsg.setValue(true);
      }
      else
      {
         statsByWord.setValue(true);
      }
   }

   @Override
   public HasPager getPageNavigation()
   {
      return pager;
   }

   @Override
   public void showRowLoading(int row)
   {
      documentListTable.showRowLoading(row);
   }
}
