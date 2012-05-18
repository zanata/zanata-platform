/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter.TransUnitReplaceInfo;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.SearchResultsDocumentTable;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

/**
 * UI for project-wide search & replace
 * 
 * @author David Mason, damason@redhat.com
 */
public class SearchResultsView extends Composite implements SearchResultsPresenter.Display
{

   private static SearchResultsViewUiBinder uiBinder = GWT.create(SearchResultsViewUiBinder.class);

   interface SearchResultsViewUiBinder extends UiBinder<LayoutPanel, SearchResultsView>
   {
   }

   @UiField
   VerticalPanel searchResultsPanel;

   @UiField
   TextBox filterTextBox, replacementTextBox;

   @UiField
   InlineLabel searchResponseLabel, selectAllLink, replaceAllFeedbackLabel, replaceAllUndoLabel;

   @UiField
   CheckBox caseSensitiveChk;

   @UiField
   Button replaceAllButton;

   @UiField
   ListBox searchFieldsSelect;

   private final WebTransMessages messages;

   @Inject
   public SearchResultsView(Resources resources, final WebTransMessages webTransMessages)
   {
      messages = webTransMessages;
      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setHighlightString(String highlightString)
   {
      SearchResultsDocumentTable.setHighlightString(highlightString);
   }

   @Override
   public HasValue<String> getFilterTextBox()
   {
      return filterTextBox;
   }

   @Override
   public HasValue<String> getReplacementTextBox()
   {
      return replacementTextBox;
   }

   @Override
   public HasValue<Boolean> getCaseSensitiveChk()
   {
      return caseSensitiveChk;
   }

   @Override
   public HasClickHandlers getReplaceAllButton()
   {
      return replaceAllButton;
   }

   @Override
   public HasClickHandlers getSelectAllButton()
   {
      return selectAllLink;
   }

   @Override
   public HasText getSearchResponseLabel()
   {
      return searchResponseLabel;
   }

   @Override
   public void clearAll()
   {
      searchResultsPanel.clear();
   }

   @Override
   public HasChangeHandlers getSearchFieldSelector()
   {
      return searchFieldsSelect;
   }

   @Override
   public String getSelectedSearchField()
   {
      return searchFieldsSelect.getValue(searchFieldsSelect.getSelectedIndex());
   }

   @Override
   public HandlerRegistration setReplacementMessage(String message, ClickHandler undoButtonHandler)
   {
      replaceAllFeedbackLabel.setText(message);
      replaceAllFeedbackLabel.setVisible(true);

      replaceAllUndoLabel.setVisible(true);
      return replaceAllUndoLabel.addClickHandler(undoButtonHandler);
   }

   @Override
   public void clearReplacementMessage()
   {
      replaceAllFeedbackLabel.setVisible(false);
      replaceAllUndoLabel.setVisible(false);
   }

   @Override
   public HasData<TransUnitReplaceInfo> addDocument(String docName,
         ClickHandler viewDocClickHandler,
         ClickHandler searchDocClickHandler,
         Delegate<TransUnitReplaceInfo> replaceDelegate,
         Delegate<TransUnitReplaceInfo> undoDelegate,
         SelectionModel<TransUnitReplaceInfo> selectionModel,
         ValueChangeHandler<Boolean> selectAllHandler)
   {
      addDocumentLabel(docName, viewDocClickHandler, searchDocClickHandler, selectAllHandler);
      SearchResultsDocumentTable table = new SearchResultsDocumentTable(replaceDelegate, undoDelegate, selectionModel, selectAllHandler, messages);
      searchResultsPanel.add(table);
      table.addStyleName("projectWideSearchResultsDocumentBody");
      return table;
   }

   private void addDocumentLabel(String docName, ClickHandler viewDocClickHandler, ClickHandler searchDocClickHandler, ValueChangeHandler<Boolean> selectAllHandler)
   {
      FlowPanel docHeading = new FlowPanel();
      docHeading.addStyleName("projectWideSearchResultsDocumentHeader");

      InlineLabel docLabel = new InlineLabel(docName);
      docLabel.addStyleName("projectWideSearchResultsDocumentTitle");
      docHeading.add(docLabel);

      CheckBox selectWholeDocCheckBox = new CheckBox("Select entire document");
      selectWholeDocCheckBox.setTitle("Select or deselect all matching text flows in this document");
      selectWholeDocCheckBox.addValueChangeHandler(selectAllHandler);
      docHeading.add(selectWholeDocCheckBox);

      InlineLabel searchDocLabel = new InlineLabel(messages.searchDocInEditor());
      searchDocLabel.setTitle(messages.searchDocInEditorDetailed());
      searchDocLabel.addClickHandler(searchDocClickHandler);
      searchDocLabel.addStyleName("linkLabel");
      searchDocLabel.addStyleName("projectWideSearchResultsDocumentLink");
      docHeading.add(searchDocLabel);

      InlineLabel showDocLabel = new InlineLabel(messages.viewDocInEditor());
      showDocLabel.setTitle(messages.viewDocInEditorDetailed());
      showDocLabel.addStyleName("linkLabel");
      showDocLabel.addStyleName("projectWideSearchResultsDocumentLink");
      showDocLabel.addClickHandler(viewDocClickHandler);
      docHeading.add(showDocLabel);

      searchResultsPanel.add(docHeading);
   }

}
