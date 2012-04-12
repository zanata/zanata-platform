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

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.ClearableTextBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
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
   public HasData<TransUnit> addTUTable(Delegate<TransUnit> replaceDelegate)
   {
      CellTable<TransUnit> table = buildTable(replaceDelegate);
      searchResultsPanel.add(table);
      return table;
   }

   private CellTable<TransUnit> buildTable(Delegate<TransUnit> replaceDelegate)
   {
      //create columns
      TextColumn<TransUnit> rowIndexColumn = new TextColumn<TransUnit>()
      {
         @Override
         public String getValue(TransUnit tu)
         {
            return Integer.toString(tu.getRowIndex());
         }
      };

      TextColumn<TransUnit> sourceColumn = new TextColumn<TransUnit>()
      {
         @Override
         public String getValue(TransUnit tu)
         {
            //FIXME update to use plurals
            return tu.getSources().get(0);
         }
      };

      Column<TransUnit, String> targetColumn = new Column<TransUnit, String>(new AbstractCell<String>() {

         @Override
         public void render(com.google.gwt.cell.client.Cell.Context context, String value, SafeHtmlBuilder sb)
         {
            HighlightingLabel label = new HighlightingLabel(value);
            if (highlightString != null && highlightString.length() > 0)
            {
               label.highlightSearch(highlightString);
            }
            sb.appendHtmlConstant(label.getElement().getString());
         }

      })
      {

         @Override
         public String getValue(TransUnit tu)
         {
            //FIXME update to use plurals
            return tu.getTargets().get(0);
         }

         @Override
         public String getCellStyleNames(Context context, TransUnit tu)
         {
            String styleNames = super.getCellStyleNames(context, tu);
            if (styleNames == null)
            {
               styleNames = "";
            }
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
      table.setWidth("100%", true);

      //TODO use localisable headings (should already exist somewhere)
      table.addColumn(rowIndexColumn, "Index");
      table.addColumn(sourceColumn, "Source");
      table.addColumn(targetColumn, "Target");
      table.addColumn(replaceButtonColumn, "Actions");

      table.setColumnWidth(rowIndexColumn, 70.0, Unit.PX);
      table.setColumnWidth(sourceColumn, 50.0, Unit.PCT);
      table.setColumnWidth(targetColumn, 50.0, Unit.PCT);
      table.setColumnWidth(replaceButtonColumn, 100.0, Unit.PX);

      return table;
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
