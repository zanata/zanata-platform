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

import java.util.Collection;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter.TransUnitReplaceInfo;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HighlightingLabel;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
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

   private String highlightString;

   private final WebTransMessages messages;

   @Inject
   public SearchResultsView(Resources resources, final WebTransMessages webTransMessages)
   {
      highlightString = null;
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
      this.highlightString = highlightString;
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
      CellTable<TransUnitReplaceInfo> table = buildTable(replaceDelegate, undoDelegate, selectionModel, selectAllHandler);
      searchResultsPanel.add(table);
      table.addStyleName("projectWideSearchResultsDocumentBody");
      return table;
   }

   public void addDocumentLabel(String docName, ClickHandler viewDocClickHandler, ClickHandler searchDocClickHandler, ValueChangeHandler<Boolean> selectAllHandler)
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

      // TODO disabled until source-and-target search in editor responds properly to history
      // TODO localizable strings
//      InlineLabel searchDocLabel = new InlineLabel("Search document in editor");
//      searchDocLabel.setTitle("Show this document in the editor with the current search active");
//      searchDocLabel.addClickHandler(searchDocClickHandler);
//      searchDocLabel.addStyleName("linkLabel");
//      searchDocLabel.addStyleName("projectWideSearchResultsDocumentLink");
//      docHeading.add(searchDocLabel);

      InlineLabel showDocLabel = new InlineLabel(messages.viewDocInEditor());
      showDocLabel.setTitle(messages.viewDocInEditorDetailed());
      showDocLabel.addStyleName("linkLabel");
      showDocLabel.addStyleName("projectWideSearchResultsDocumentLink");
      showDocLabel.addClickHandler(viewDocClickHandler);
      docHeading.add(showDocLabel);

      searchResultsPanel.add(docHeading);
   }

   private CellTable<TransUnitReplaceInfo> buildTable(Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate, SelectionModel<TransUnitReplaceInfo> selectionModel,
         ValueChangeHandler<Boolean> selectAllHandler)
   {
      // create columns
      CheckColumn checkboxColumn = new CheckColumn(selectionModel);

      TextColumn<TransUnitReplaceInfo> rowIndexColumn = new TextColumn<TransUnitReplaceInfo>()
      {
         @Override
         public String getValue(TransUnitReplaceInfo tu)
         {
            return Integer.toString(tu.getTransUnit().getRowIndex() + 1);
         }
      };

      Column<TransUnitReplaceInfo, List<String>> sourceColumn = new Column<TransUnitReplaceInfo, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {

            for (String source : notEmptyContents(contents))
            {
               HighlightingLabel label = new HighlightingLabel(source);
               if (!Strings.isNullOrEmpty(highlightString))
               {
                  label.highlightSearch(highlightString);
               }
               appendContent(sb, label.getElement().getString());
            }
         }
      })
      {
         @Override
         public List<String> getValue(TransUnitReplaceInfo info)
         {
            return info.getTransUnit().getSources();
         }
      };

      Column<TransUnitReplaceInfo, List<String>> targetColumn = new Column<TransUnitReplaceInfo, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {

            for (String target : notEmptyContents(contents))
            {
               // TODO switch to diff mode if this is a preview or there has been a replacement
               HighlightingLabel label = new HighlightingLabel(target);
               if (!Strings.isNullOrEmpty(highlightString))
               {
                  label.highlightSearch(highlightString);
               }
               appendContent(sb, label.getElement().getString());
            }
         }
      })
      {

         @Override
         public List<String> getValue(TransUnitReplaceInfo info)
         {
            return info.getTransUnit().getTargets();
         }

         @Override
         public String getCellStyleNames(Context context, TransUnitReplaceInfo info)
         {
            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, info));
            if (info.getTransUnit().getStatus() == ContentState.Approved)
            {
               styleNames += " ApprovedStateDecoration";
            }
            else if (info.getTransUnit().getStatus() == ContentState.NeedReview)
            {
               styleNames += " FuzzyStateDecoration";
            }
            return styleNames;
         }
      };

      ReplaceColumn replaceButtonColumn = new ReplaceColumn(replaceDelegate, undoDelegate);

      CellTable<TransUnitReplaceInfo> table = new CellTable<TransUnitReplaceInfo>();
      table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<TransUnitReplaceInfo> createCheckboxManager());
      table.setWidth("100%", true);

      table.addColumn(checkboxColumn, "");
      table.addColumn(rowIndexColumn, messages.rowIndex());
      table.addColumn(sourceColumn, messages.source());
      table.addColumn(targetColumn, messages.target());
      table.addColumn(replaceButtonColumn, messages.actions());

      sourceColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      targetColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

      table.setColumnWidth(checkboxColumn, 50.0, Unit.PX);
      table.setColumnWidth(rowIndexColumn, 70.0, Unit.PX);
      table.setColumnWidth(sourceColumn, 50.0, Unit.PCT);
      table.setColumnWidth(targetColumn, 50.0, Unit.PCT);
      table.setColumnWidth(replaceButtonColumn, 100.0, Unit.PX);

      table.addStyleName("projectWideSearchResultsDocumentBody");
      return table;
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div class='translationContainer' style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }

   private static Collection<String> notEmptyContents(List<String> contents)
   {
      return Collections2.filter(contents, new Predicate<String>()
      {
         @Override
         public boolean apply(String input)
         {
            return !Strings.isNullOrEmpty(input);
         }
      });
   }

   private class CheckColumn extends Column<TransUnitReplaceInfo, Boolean>
   {

      private SelectionModel<TransUnitReplaceInfo> selectionModel;

      public CheckColumn(SelectionModel<TransUnitReplaceInfo> selectionModel)
      {
         super(new CheckboxCell(true, false));
         this.selectionModel = selectionModel;
      }

      @Override
      public Boolean getValue(TransUnitReplaceInfo info)
      {
         return selectionModel.isSelected(info);
      }

   }

   private class ReplaceColumn extends Column<TransUnitReplaceInfo, TransUnitReplaceInfo>
   {

      public ReplaceColumn(Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate)
      {
         super(new UndoableTransUnitActionCell("Replace", replaceDelegate, "Undo", undoDelegate));
      }

      @Override
      public TransUnitReplaceInfo getValue(TransUnitReplaceInfo info)
      {
         return info;
      }
   }

   private class UndoableTransUnitActionCell extends ActionCell<TransUnitReplaceInfo>
   {
      private Delegate<TransUnitReplaceInfo> undoDelegate;
      private final SafeHtml undoHtml;

      public UndoableTransUnitActionCell(SafeHtml actionLabel, Delegate<TransUnitReplaceInfo> actionDelegate, SafeHtml undoLabel, Delegate<TransUnitReplaceInfo> undoDelegate) {
         super(actionLabel, actionDelegate);
         this.undoDelegate = undoDelegate;
         // TODO make this a 'replaced' message with undo button or link
         this.undoHtml = new SafeHtmlBuilder().appendHtmlConstant(
             "<button type=\"button\" tabindex=\"-1\">").append(undoLabel).appendHtmlConstant(
             "</button>").toSafeHtml();
      }

      public UndoableTransUnitActionCell(String actionLabel, Delegate<TransUnitReplaceInfo> actionDelegate, String undoLabel, Delegate<TransUnitReplaceInfo> undoDelegate)
      {
         this(SafeHtmlUtils.fromString(actionLabel), actionDelegate, SafeHtmlUtils.fromString(undoLabel), undoDelegate);
      }

      @Override
      public void render(com.google.gwt.cell.client.Cell.Context context, TransUnitReplaceInfo value, SafeHtmlBuilder sb)
      {

         if (value.isReplaceable())
         {
            // render default button
            super.render(context, value, sb);
         }
         else if (value.isUndoable())
         {
            // render undo button
            sb.append(undoHtml);
         }
         else
         {
            //assume processing
            // TODO set to show "replacing..." or "processing..."
         }

      }

      @Override
      protected void onEnterKeyDown(Context context, Element parent, TransUnitReplaceInfo value, NativeEvent event, ValueUpdater<TransUnitReplaceInfo> valueUpdater) {
         // FIXME see above
         if (value.isReplaceable())
         {
            super.onEnterKeyDown(context, parent, value, event, valueUpdater);
         }
         else if (value.isUndoable())
         {
            undoDelegate.execute(value);
         }
         // else ignore (is processing)
      };
   }

}
