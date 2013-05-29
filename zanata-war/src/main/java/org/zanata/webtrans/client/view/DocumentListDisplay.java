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
package org.zanata.webtrans.client.view;

import java.util.HashMap;
import java.util.List;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.rest.dto.stats.CommonContainerTranslationStatistics;
import org.zanata.webtrans.client.ui.DocumentListTable.DocValidationStatus;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.ui.SearchFieldListener;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.WorkspaceId;

import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public interface DocumentListDisplay extends WidgetDisplay, SearchFieldListener
{
   public static String PATH_HEADER = "path";
   public static String DOC_HEADER = "doc";
   public static String STATS_HEADER = "stats";
   public static String TRANSLATED_HEADER = "translated";
   public static String UNTRANSLATED_HEADER = "untranslated";
   public static String REMAINING_HEADER = "remaining";
   public static String LAST_UPLOAD_HEADER = "lastUpload";
   public static String LAST_TRANSLATED_HEADER = "lastTranslated";

   public static final String STATS_OPTION_WORDS = "Words";
   public static final String STATS_OPTION_MESSAGE = "Message";

   String getSelectedStatsOption();

   void setListener(Listener documentListPresenter);

   void updateFilter(boolean docFilterCaseSensitive, boolean docFilterExact, String docFilterText);

   interface Listener
   {
      void statsOptionChange();

      void fireCaseSensitiveToken(boolean value);

      void fireExactSearchToken(boolean value);

      void fireFilterToken(String value);

      void fireDocumentSelection(DocumentInfo docInfo);

      void downloadAllFiles();

      void showUploadDialog(DocumentInfo docInfo);

      void cancelDownloadAllFiles();

      void cancelFileUpload();

      void onFileUploadComplete(SubmitCompleteEvent event);

      void onUploadFile();

      void updateDownloadFileProgress();

      void sortList(String header, boolean asc);

      void pagerValueChanged(Integer value);
   }

   void setLayout(String layout);

   void hideConfirmation();

   void updateFileDownloadProgress(int currentProgress, int maxProgress);

   void setDownloadInProgress(boolean inProgress);

   void setAndShowFilesDownloadLink(String url);

   InlineLink getDownloadAllFilesInlineLink(String url);

   void showUploadDialog(DocumentInfo info, WorkspaceId workspaceId);

   void closeFileUpload();

   String getSelectedUploadFileName();

   void submitUploadForm();

   void startGetDownloadStatus(int periodMillis);

   void stopGetDownloadStatus();

   void setDownloadZipButtonText(String text);

   void setDownloadZipButtonTitle(String title);

   void setEnableDownloadZip(boolean enabled);

   void showLoading(boolean showLoading);

   HashMap<DocumentId, Integer> buildContent(List<DocumentNode> nodes);

   void updateStats(int row, CommonContainerTranslationStatistics stats);

   void setStatsFilters(Integer row);

   HasPager getPageNavigation();

   void setStatsFilters(String option);

   void showRowLoading(int row);

   void updateRowHasError(int row, DocValidationStatus status);

   void updateLastTranslated(int intValue, AuditInfo lastTranslated);
}
