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

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.CellTableResources;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitProvidesKey;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitListTable extends CellTable<TransUnit> implements TransUnitListDisplay
{

   private static CellTableResources cellTableResources;
//   private Resources resources;
//   private WebTransMessages messages;

   private String highlightString = null;


   @Override
   public void setHighlightString(String highlightString)
   {
      this.highlightString = highlightString;
   }

   @Inject
   public TransUnitListTable(WebTransMessages messages)
   {
      //TODO page size should be configurable
      super(15, getCellTableResources());


//      this.messages = messages;
//      this.resources = resources;

      final TextColumn<TransUnit> rowIndexColumn = buildRowIndexColumn();
      final Column<TransUnit, List<String>> sourceColumn = buildSourceColumn();
      final Column<TransUnit, List<String>> targetColumn = buildTargetColumn();

      setWidth("100%", true);

      addColumn(rowIndexColumn, messages.rowIndex());
      addColumn(sourceColumn, messages.source());
      addColumn(targetColumn, messages.target());

      setColumnWidth(rowIndexColumn, 70.0, com.google.gwt.dom.client.Style.Unit.PX);
      setColumnWidth(sourceColumn, 50.0, com.google.gwt.dom.client.Style.Unit.PCT);
      setColumnWidth(targetColumn, 50.0, com.google.gwt.dom.client.Style.Unit.PCT);

      sourceColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      targetColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

      setSelectionModel(new SingleSelectionModel<TransUnit>(TransUnitProvidesKey.KEY_PROVIDER));

      //TODO change style
      addStyleName("projectWideSearchResultsDocumentBody");
   }


   /**
    * @return a column that displays the 1-based index of the text flow
    */
   private static TextColumn<TransUnit> buildRowIndexColumn()
   {
      return new TextColumn<TransUnit>()
      {
         @Override
         public String getValue(TransUnit transUnit)
         {
            return Integer.toString(transUnit.getRowIndex() + 1);
         }
      };
   }


   /**
    * @return a column that displays the source contents for the text flow
    */
   private Column<TransUnit, List<String>> buildSourceColumn()
   {
      return new Column<TransUnit, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {
            for (String source : contents)
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
         public List<String> getValue(TransUnit transUnit)
         {
            return transUnit.getSources();
         }
      };
   }

   /**
    * @return a column that displays the target contents for the text flow
    */
   private Column<TransUnit, List<String>> buildTargetColumn()
   {
      return new Column<TransUnit, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> targetContents, SafeHtmlBuilder sb)
         {
            for (String target : targetContents)
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
         public List<String> getValue(TransUnit transUnit)
         {
            return transUnit.getTargets();
         }

         @Override
         public String getCellStyleNames(Cell.Context context, TransUnit transUnit)
         {
            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, transUnit));
            if (transUnit.getStatus() == ContentState.Approved)
            {
               styleNames += " ApprovedStateDecoration";
            }
            else if (transUnit.getStatus() == ContentState.NeedReview)
            {
               styleNames += " FuzzyStateDecoration";
            }
            return styleNames;
         }
      };
   }


   private static CellTableResources getCellTableResources()
   {
      if (cellTableResources == null)
      {
         cellTableResources = GWT.create(CellTableResources.class);
      }
      return cellTableResources;
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div class='translationContainer' style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }

}
