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
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;

public class SearchResultsView extends Composite implements SearchResultsPresenter.Display
{

   private static DocumentListViewUiBinder uiBinder = GWT.create(DocumentListViewUiBinder.class);

   interface DocumentListViewUiBinder extends UiBinder<LayoutPanel, SearchResultsView>
   {
   }

   @UiField
   VerticalPanel searchResultsPanel;

   @UiField(provided = true)
   ClearableTextBox filterTextBox, replacementTextBox;

   @UiField
   Label testLabel;

   @Inject
   public SearchResultsView(Resources resources, UiMessages uiMessages, final CachingDispatchAsync dispatcher, EventBus eventBus)
   {
      filterTextBox = new ClearableTextBox(resources, uiMessages);
      replacementTextBox = new ClearableTextBox(resources, uiMessages);

      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public Widget asWidget()
   {
      return this;
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
      docLabel.getElement().setAttribute("style", "cursor:pointer;");
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
      CellTable<TransUnit> table = new CellTable<TransUnit>();

      TextColumn<TransUnit> rowIndexColumn = new TextColumn<TransUnit>()
      {
         @Override
         public String getValue(TransUnit tu)
         {
            return new Integer(tu.getRowIndex()).toString();
         }
      };

      TextColumn<TransUnit> sourceColumn = new TextColumn<TransUnit>()
      {
         @Override
         public String getValue(TransUnit tu)
         {
            return tu.getSource();
         }
      };

      //TODO highlight search match
      TextColumn<TransUnit> targetColumn = new TextColumn<TransUnit>()
      {

         @Override
         public String getValue(TransUnit tu)
         {
            return tu.getTarget();
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

      //TODO use localisable headings (should already exist somewhere)
      table.addColumn(rowIndexColumn, "Index");
      table.addColumn(sourceColumn, "Source");
      table.addColumn(targetColumn, "Target");
      table.addColumn(replaceButtonColumn, "Actions");

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