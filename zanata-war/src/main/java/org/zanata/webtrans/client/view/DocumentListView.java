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

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DocumentListTable;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.HasStatsFilter;
import org.zanata.webtrans.client.ui.SearchField;
import org.zanata.webtrans.client.ui.table.DocumentListPager;
import org.zanata.webtrans.shared.model.DocumentInfo;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;

public class DocumentListView extends Composite implements DocumentListDisplay, HasSelectionHandlers<DocumentInfo>
{
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

   @UiField(provided = true)
   DocumentListPager pager;

   private DocumentListTable documentListTable;

   private final Resources resources;
   private final WebTransMessages messages;

   private ListDataProvider<DocumentNode> dataProvider;

   @Inject
   public DocumentListView(Resources resources, WebTransMessages messages)
   {
      this.resources = resources;
      this.messages = messages;

      dataProvider = new ListDataProvider<DocumentNode>();

      pager = new DocumentListPager(TextLocation.CENTER, false, true);
      searchField = new SearchField(this);
      searchField.setTextBoxTitle(messages.docListFilterDescription());

      initWidget(uiBinder.createAndBindUi(this));
      
      caseSensitiveCheckBox.setTitle(messages.docListFilterCaseSensitiveDescription());
      exactSearchCheckBox.setTitle(messages.docListFilterExactMatchDescription());
      statsByMsg.setText(messages.byMessage());
      statsByWord.setText(messages.byWords());
      this.addSelectionHandler(new SelectionHandler<DocumentInfo>()
      {
         @Override
         public void onSelection(SelectionEvent<DocumentInfo> event)
         {
            listener.fireDocumentSelection(event.getSelectedItem());
         }
      });
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
         return HasStatsFilter.STATS_OPTION_MESSAGE;
      }
      else
      {
         return HasStatsFilter.STATS_OPTION_WORDS;
      }
   }

   @Override
   public HasData<DocumentNode> getDocumentListTable()
   {
      return documentListTable;
   }

   @Override
   public HandlerRegistration addSelectionHandler(SelectionHandler<DocumentInfo> handler)
   {
      return addHandler(handler, SelectionEvent.getType());
   }

   @Override
   public ListDataProvider<DocumentNode> getDataProvider()
   {
      return dataProvider;
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
         listener.statsOptionChange(HasStatsFilter.STATS_OPTION_MESSAGE);
      }
   }

   @UiHandler("statsByWord")
   public void onStatsByWordChange(ValueChangeEvent<Boolean> event)
   {
      if (event.getValue())
      {
         listener.statsOptionChange(HasStatsFilter.STATS_OPTION_WORDS);
      }
   }

   @Override
   public void setStatsFilter(String option)
   {
      if (option.equals(HasStatsFilter.STATS_OPTION_MESSAGE))
      {
         statsByMsg.setValue(true);
      }
      else
      {
         statsByWord.setValue(true);
      }
      documentListTable.setStatsFilter(option);
   }

   interface DocumentListViewUiBinder extends UiBinder<LayoutPanel, DocumentListView>
   {
   }

   @Override
   public void setThemes(String theme)
   {
      documentListContainer.setStyleName(theme);
   }

   @Override
   public void updateFilter(boolean docFilterCaseSensitive, boolean docFilterExact, String docFilterText)
   {
      caseSensitiveCheckBox.setValue(docFilterCaseSensitive, false);
      exactSearchCheckBox.setValue(docFilterExact, false);
      searchField.setText(docFilterText);
   }

   @Override
   public void renderTable(SingleSelectionModel<DocumentNode> selectionModel)
   {
      documentListTable = new DocumentListTable(resources, messages, dataProvider, selectionModel);
      dataProvider.addDataDisplay(documentListTable);

      documentListContainer.clear();
      documentListContainer.add(documentListTable);
   }

   @Override
   public HasSelectionHandlers<DocumentInfo> getDocumentList()
   {
      return this;
   }

   @Override
   public void setListener(Listener documentListPresenter)
   {
      this.listener = documentListPresenter;
   }

   @Override
   public void updatePageSize(int pageSize)
   {
      documentListTable.setPageSize(pageSize);
      pager.setDisplay(documentListTable);
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
}