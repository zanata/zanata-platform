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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.SearchFieldListener;
import org.zanata.webtrans.shared.model.DocumentInfo;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public interface DocumentListDisplay extends WidgetDisplay, SearchFieldListener
{
   void updatePageSize(int pageSize);

   HasSelectionHandlers<DocumentInfo> getDocumentList();

   HasData<DocumentNode> getDocumentListTable();

   ListDataProvider<DocumentNode> getDataProvider();

   void renderTable(SingleSelectionModel<DocumentNode> selectionModel);

   String getSelectedStatsOption();

   void setStatsFilter(String option);

   void setListener(Listener documentListPresenter);

   void updateFilter(boolean docFilterCaseSensitive, boolean docFilterExact, String docFilterText);

   interface Listener
   {
      void statsOptionChange(String option);

      void fireCaseSensitiveToken(boolean value);

      void fireExactSearchToken(boolean value);

      void fireFilterToken(String value);

      void fireDocumentSelection(DocumentInfo doc);

      void downloadAllFiles();
   }

   void setThemes(String style);

   void hideConfirmation();

   void updateFileDownloadProgress(int currentProgress, int maxProgress);
}
