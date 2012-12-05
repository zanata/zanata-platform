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

import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TransUnitReplaceInfo;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DiffColorLegendPanel;
import org.zanata.webtrans.client.ui.LoadingPanel;
import org.zanata.webtrans.client.ui.SearchResultsDocumentTable;

import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
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
   InlineLabel searchResponseLabel;

   @UiField
   CheckBox caseSensitiveChk, selectAllChk, requirePreviewChk;

   @UiField
   Button searchButton, replaceAllButton;

   @UiField
   ListBox searchFieldsSelect;

   private final LoadingPanel searchingIndicator;

   Label noResultsLabel;

   private final WebTransMessages messages;

   private Resources resources;

   private HasValue<Boolean> selectAllCheckbox;

   private final DiffColorLegendPanel diffLegendPanel;

   @Inject
   public SearchResultsView(Resources resources, final WebTransMessages webTransMessages, final DiffColorLegendPanel diffLegendPanel, LoadingPanel loadingPanel)
   {
      messages = webTransMessages;
      this.resources = resources;
      this.diffLegendPanel = diffLegendPanel;
      initWidget(uiBinder.createAndBindUi(this));
      searchingIndicator = loadingPanel;
      noResultsLabel = new Label(messages.noSearchResults());
      noResultsLabel.addStyleName("projectWideSearchNoResultsLabel");
      searchResultsPanel.add(noResultsLabel);
      requirePreviewChk.setValue(true, false);
      requirePreviewChk.setTitle(messages.requirePreviewDescription());
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
   public void focusFilterTextBox()
   {
      Scheduler.get().scheduleDeferred(new Command()
      {
         @Override
         public void execute()
         {
            filterTextBox.setFocus(true);
            filterTextBox.setSelectionRange(0, filterTextBox.getText().length());
         }
      });
   }

   @UiHandler("filterTextBox")
   void onFilterTextBoxKeyPress(KeyPressEvent event)
   {
      if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER)
      {
         searchButton.click();
      }
   }

   @Override
   public HasClickHandlers getSearchButton()
   {
      return searchButton;
   }

   @Override
   public HasValue<String> getReplacementTextBox()
   {
      return replacementTextBox;
   }

   @Override
   public void focusReplacementTextBox()
   {
      Scheduler.get().scheduleDeferred(new Command()
      {
         @Override
         public void execute()
         {
            replacementTextBox.setFocus(true);
            replacementTextBox.setSelectionRange(0, replacementTextBox.getText().length());
         }
      });
   }

   @Override
   public HasValue<Boolean> getCaseSensitiveChk()
   {
      return caseSensitiveChk;
   }

   @Override
   public HasValue<Boolean> getSelectAllChk()
   {
      return selectAllChk;
   }

   @Override
   public HasClickHandlers getReplaceAllButton()
   {
      return replaceAllButton;
   }

   @Override
   public void setReplaceAllButtonEnabled(boolean enabled)
   {
      replaceAllButton.setEnabled(enabled);
      if (enabled)
      {
         replaceAllButton.removeStyleName("projectWideReplacButton-Disabled");
         replaceAllButton.setTitle(messages.replaceSelectedDescription());
      }
      else
      {
         replaceAllButton.addStyleName("projectWideReplacButton-Disabled");
         replaceAllButton.setTitle(messages.replaceSelectedDisabledDescription());
      }
   }

   @Override
   public void setReplaceAllButtonVisible(boolean visible)
   {
      replaceAllButton.setVisible(visible);
   }

   @Override
   public HasValue<Boolean> getRequirePreviewChk()
   {
      return requirePreviewChk;
   }

   @Override
   public void setRequirePreview(boolean required)
   {
      requirePreviewChk.setValue(required, false);
      SearchResultsDocumentTable.setRequirePreview(required);
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
      searchResultsPanel.add(noResultsLabel);
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
   public ListDataProvider<TransUnitReplaceInfo> addDocument(String docName, ClickHandler viewDocClickHandler, ClickHandler searchDocClickHandler, ClickHandler infoClickHandler, MultiSelectionModel<TransUnitReplaceInfo> selectionModel, ValueChangeHandler<Boolean> selectAllHandler)
   {
      SearchResultsDocumentTable table = new SearchResultsDocumentTable(selectionModel, selectAllHandler, messages);
      return addDocument(docName, viewDocClickHandler, searchDocClickHandler, infoClickHandler, table);
   }

   @Override
   public ListDataProvider<TransUnitReplaceInfo> addDocument(String docName, ClickHandler viewDocClickHandler, ClickHandler searchDocClickHandler, ClickHandler infoClickHandler, MultiSelectionModel<TransUnitReplaceInfo> selectionModel, ValueChangeHandler<Boolean> selectAllHandler, Delegate<TransUnitReplaceInfo> previewDelegate, Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate)
   {
      SearchResultsDocumentTable table = new SearchResultsDocumentTable(previewDelegate, replaceDelegate, undoDelegate, selectionModel, selectAllHandler, messages, resources);
      return addDocument(docName, viewDocClickHandler, searchDocClickHandler, infoClickHandler, table);
   }

   /**
    * @param docName
    * @param viewDocClickHandler
    * @param searchDocClickHandler
    * @param table
    * @return
    */
   private ListDataProvider<TransUnitReplaceInfo> addDocument(String docName, ClickHandler viewDocClickHandler, ClickHandler searchDocClickHandler, ClickHandler infoClickHandler, SearchResultsDocumentTable table)
   {
      // ensure 'no results' message is no longer visible
      noResultsLabel.removeFromParent();
      addDocumentLabel(docName, viewDocClickHandler, searchDocClickHandler, infoClickHandler);
      searchResultsPanel.add(table);
      table.addStyleName("projectWideSearchResultsDocumentBody");

      ListDataProvider<TransUnitReplaceInfo> dataProvider = new ListDataProvider<TransUnitReplaceInfo>();
      dataProvider.addDataDisplay(table);
      selectAllCheckbox = table.getCheckbox();
      return dataProvider;
   }

   @Override
   public HasValue<Boolean> getSelectAllCheckbox()
   {
      return selectAllCheckbox;
   }

   private void addDocumentLabel(String docName, ClickHandler viewDocClickHandler, ClickHandler searchDocClickHandler, ClickHandler infoClickHandler)
   {
      FlowPanel docHeading = new FlowPanel();
      docHeading.addStyleName("projectWideSearchResultsDocumentHeader");

      InlineLabel docLabel = new InlineLabel(docName);
      docLabel.addStyleName("projectWideSearchResultsDocumentTitle");
      docHeading.add(docLabel);

      InlineLabel searchDocLabel = new InlineLabel(messages.searchDocInEditor());
      searchDocLabel.setTitle(messages.searchDocInEditorDetailed());
      searchDocLabel.addClickHandler(searchDocClickHandler);
      searchDocLabel.addStyleName("linkLabel");
      searchDocLabel.addStyleName("linkLabelNormalColor");
      searchDocLabel.addStyleName("projectWideSearchResultsDocumentLink");
      docHeading.add(searchDocLabel);

      InlineLabel showDocLabel = new InlineLabel(messages.viewDocInEditor());
      showDocLabel.setTitle(messages.viewDocInEditorDetailed());
      showDocLabel.addStyleName("linkLabel");
      showDocLabel.addStyleName("linkLabelNormalColor");
      showDocLabel.addStyleName("projectWideSearchResultsDocumentLink");
      showDocLabel.addClickHandler(viewDocClickHandler);
      docHeading.add(showDocLabel);

      InlineLabel infoLabel = new InlineLabel();
      infoLabel.setStyleName("icon-info-circle-2");
      infoLabel.setTitle(messages.colorLegend());
      infoLabel.addClickHandler(infoClickHandler);
      infoLabel.addStyleName("projectWideSearchResultsInfo");
      infoLabel.addStyleName("linkLabelNormalColor");
      infoLabel.addStyleName("projectWideSearchResultsDocumentLink");
      docHeading.add(infoLabel);

      searchResultsPanel.add(docHeading);
   }

   @Override
   public void setSearching(boolean searching)
   {
      if (searching)
      {
         searchingIndicator.center();
      }
      else
      {
         searchingIndicator.hide();
      }
   }

   @Override
   public MultiSelectionModel<TransUnitReplaceInfo> createMultiSelectionModel()
   {
      return new MultiSelectionModel<TransUnitReplaceInfo>();
   }

   @Override
   public void addSearchFieldsSelect(String item, String value)
   {
      searchFieldsSelect.addItem(item, value);
   }

   @Override
   public void showDiffLegend()
   {
      diffLegendPanel.show(ShortcutContext.ProjectWideSearch);
   }
}
