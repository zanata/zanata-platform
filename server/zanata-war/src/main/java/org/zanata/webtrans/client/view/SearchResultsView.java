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
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.ClearableTextBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
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

   @UiField(provided = true)
   ClearableTextBox filterTextBox, replacementTextBox;

   @UiField
   Label testLabel;

   @UiField
   CheckBox caseSensitiveChk;

   @UiField
   Button replaceAllButton;

   private String highlightString;

   @Inject
   public SearchResultsView(Resources resources, UiMessages uiMessages)
   {
      filterTextBox = new ClearableTextBox(resources, uiMessages);
      replacementTextBox = new ClearableTextBox(resources, uiMessages);
      highlightString = null;

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
      return filterTextBox.getTextBox();
   }

   @Override
   public HasValue<String> getReplacementTextBox()
   {
      return replacementTextBox.getTextBox();
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
   public HasText getTestLabel()
   {
      return testLabel;
   }

   @Override
   public void clearAll()
   {
      searchResultsPanel.clear();
   }

   public HasClickHandlers addDocumentLabel(String docName)
   {
      Label docLabel = new Label(docName);
      searchResultsPanel.add(docLabel);
      docLabel.setTitle("View document in editor");
      docLabel.addStyleName("SearchResultsDocumentTitle");
      return docLabel;
   }

   @Override
   public HasData<TransUnit> addTUTable(Delegate<TransUnit> replaceDelegate,
                             SelectionModel<TransUnit> selectionModel,
                             ValueChangeHandler<Boolean> selectAllHandler)
   {
      CellTable<TransUnit> table = buildTable(replaceDelegate, selectionModel, selectAllHandler);
      searchResultsPanel.add(table);
      return table;
   }

   private CellTable<TransUnit> buildTable(Delegate<TransUnit> replaceDelegate, SelectionModel<TransUnit> selectionModel,
         ValueChangeHandler<Boolean> selectAllHandler)
   {
      // create columns
      CheckColumn checkboxColumn = new CheckColumn(selectionModel);
      CheckboxHeader checkboxColumnHeader = new CheckboxHeader();
      checkboxColumnHeader.addValueChangeHandler(selectAllHandler);

      TextColumn<TransUnit> rowIndexColumn = new TextColumn<TransUnit>()
      {
         @Override
         public String getValue(TransUnit tu)
         {
            return Integer.toString(tu.getRowIndex());
         }
      };

      Column<TransUnit, List<String>> sourceColumn = new Column<TransUnit, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {
            for (String source : notEmptyContents(contents))
            {
               //we use highlighting label here just so source and target contents can line up nicely
               HighlightingLabel label = new HighlightingLabel(source);
               appendContent(sb, label.getElement().getString());
            }
         }
      })
      {
         @Override
         public List<String> getValue(TransUnit transUnit)
         {
            return transUnit.getSources();
         }
      };

      Column<TransUnit, List<String>> targetColumn = new Column<TransUnit, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {

            for (String target : notEmptyContents(contents))
            {
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
         public List<String> getValue(TransUnit tu)
         {
            return tu.getTargets();
         }

         @Override
         public String getCellStyleNames(Context context, TransUnit tu)
         {
            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, tu));
            if (tu.getStatus() == ContentState.Approved)
            {
               styleNames += " ApprovedStateDecoration";
            }
            else if (tu.getStatus() == ContentState.NeedReview)
            {
               styleNames += " FuzzyStateDecoration";
            }
            return styleNames;
         }
      };

      ReplaceColumn replaceButtonColumn = new ReplaceColumn(replaceDelegate);

      CellTable<TransUnit> table = new CellTable<TransUnit>();
      table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<TransUnit> createCheckboxManager());
      table.setWidth("100%", true);

      // TODO use localisable headings (should already exist somewhere)
      table.addColumn(checkboxColumn, checkboxColumnHeader);
      table.addColumn(rowIndexColumn, "Index");
      table.addColumn(sourceColumn, "Source");
      table.addColumn(targetColumn, "Target");
      table.addColumn(replaceButtonColumn, "Actions");

      sourceColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      targetColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

      table.setColumnWidth(checkboxColumn, 50.0, Unit.PX);
      table.setColumnWidth(rowIndexColumn, 70.0, Unit.PX);
      table.setColumnWidth(sourceColumn, 50.0, Unit.PCT);
      table.setColumnWidth(targetColumn, 50.0, Unit.PCT);
      table.setColumnWidth(replaceButtonColumn, 100.0, Unit.PX);

      return table;
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
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

   private class CheckColumn extends Column<TransUnit, Boolean>
   {

      private SelectionModel<TransUnit> selectionModel;

      public CheckColumn(SelectionModel<TransUnit> selectionModel)
      {
         super(new CheckboxCell(true, false));
         this.selectionModel = selectionModel;
      }

      @Override
      public Boolean getValue(TransUnit tu)
      {
         return selectionModel.isSelected(tu);
      }

   }

   private class CheckboxHeader extends Header<Boolean> implements HasValue<Boolean> {

      private boolean checked;
      private HandlerManager handlerManager;

      public CheckboxHeader()
      {
         //TODO consider custom cell with text
         super(new CheckboxCell());
         checked = false;
      }

      // This method is invoked to pass the value to the CheckboxCell's render method
      @Override
      public Boolean getValue()
      {
         return checked;
      }

      @Override
      public void onBrowserEvent(Context context, Element elem, NativeEvent nativeEvent)
      {
         int eventType = Event.as(nativeEvent).getTypeInt();
         if (eventType == Event.ONCHANGE)
         {
            nativeEvent.preventDefault();
            //use value setter to easily fire change event to handlers
            setValue(!checked, true);
         }
      }

      @Override
      public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler)
      {
         return ensureHandlerManager().addHandler(ValueChangeEvent.getType(), handler);
      }

      @Override
      public void fireEvent(GwtEvent<?> event)
      {
         ensureHandlerManager().fireEvent(event);
      }

      @Override
      public void setValue(Boolean value)
      {
         checked = value;
      }

      @Override
      public void setValue(Boolean value, boolean fireEvents)
      {
         checked = value;
         if (fireEvents)
         {
            ValueChangeEvent.fire(this, value);
         }
      }

      private HandlerManager ensureHandlerManager()
      {
         if (handlerManager == null)
         {
            handlerManager = new HandlerManager(this);
         }
         return handlerManager;
      }
   }

   private class ReplaceColumn extends Column<TransUnit, TransUnit>
   {

      public ReplaceColumn(Delegate<TransUnit> replaceDelegate)
      {
         super(new ActionCell<TransUnit>("Replace", replaceDelegate));
      }

      @Override
      public TransUnit getValue(TransUnit tu)
      {
         return tu;
      }
   }

}
