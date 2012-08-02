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
package org.zanata.webtrans.client.editor.table;

import java.util.HashMap;
import java.util.Map;

import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.ui.TransUnitDetailsPanel;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.base.Strings;
import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.RowRenderer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TableEditorTableDefinition extends DefaultTableDefinition<TransUnit>
{

   // public static final int INDICATOR_COL = 0;
   public static final int SOURCE_COL = 0;
   public static final int TARGET_COL = 1;

   private final boolean isReadOnly;
   private final SourceContentsPresenter sourceContentsPresenter;
   private TargetContentsPresenter targetContentsPresenter;

   private String findMessage;

   private TransUnitDetailsPanel transUnitDetailsContent;

   private final RowRenderer<TransUnit> rowRenderer = new RowRenderer<TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, AbstractRowView<TransUnit> view)
      {
         String styles = "TableEditorRow ";
         String state = "";
         switch (rowValue.getStatus())
         {
         case Approved:
            state = " Approved";
            break;
         case NeedReview:
            state = " Fuzzy";
            break;
         case New:
            state = " New";
            break;
         }
         styles += state + "StateDecoration";

         view.setStyleName(styles);
      }
   };

   private final AbstractColumnDefinition<TransUnit, TransUnit> sourceColumnDefinition = new AbstractColumnDefinition<TransUnit, TransUnit>()
   {
      @Override
      public TransUnit getCellValue(TransUnit rowValue)
      {
         return rowValue;
      }

      @Override
      public void setCellValue(TransUnit rowValue, TransUnit cellValue)
      {
         cellValue.setRowIndex(rowValue.getRowIndex());
         cellValue.setSources(rowValue.getSources());
         cellValue.setSourceComment(rowValue.getSourceComment());
      }
   };

   private Map<TransUnitId, VerticalPanel> sourcePanelMap = new HashMap<TransUnitId, VerticalPanel>();

   private final CellRenderer<TransUnit, TransUnit> sourceCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(final TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Source");
         VerticalPanel panel = new VerticalPanel();
         panel.addStyleName("TableEditorCell-Source-Table");

         SourceContentsDisplay sourceContentsView = sourceContentsPresenter.setValue(rowValue);

         if (!Strings.isNullOrEmpty(findMessage))
         {
            sourceContentsView.highlightSearch(findMessage);
         }
         //row number starts with 1
         Label indexLabel = new Label(String.valueOf(rowValue.getRowIndex() + 1));
         indexLabel.setStyleName("indexLabel");
         
         panel.add(indexLabel);
         panel.add(sourceContentsView);
         // FIXME Plural support potentially this map could contain all panels
         // with all id
         sourcePanelMap.put(rowValue.getId(), panel);

         view.setWidget(panel);
      }
   };

   private final AbstractColumnDefinition<TransUnit, TransUnit> targetColumnDefinition = new AbstractColumnDefinition<TransUnit, TransUnit>()
   {

      @Override
      public TransUnit getCellValue(TransUnit rowValue)
      {
         return rowValue;
      }

      @Override
      public void setCellValue(TransUnit rowValue, TransUnit cellValue)
      {
         cellValue.setTargets(rowValue.getTargets());
      }

   };

   private final CellRenderer<TransUnit, TransUnit> targetCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, final AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Target");
         final VerticalPanel targetPanel = new VerticalPanel();

         TargetContentsDisplay contentsDisplay = targetContentsPresenter.setValue(rowValue);
         targetContentsPresenter.setToViewMode();

         targetPanel.add(contentsDisplay.asWidget());
         targetPanel.setWidth("100%");
         view.setWidget(targetPanel);
      }
   };

   private InlineTargetCellEditor targetCellEditor;

   public void setFindMessage(String findMessage)
   {
      this.findMessage = findMessage;
   }

   public TableEditorTableDefinition(final NavigationMessages messages, final RedirectingCachedTableModel<TransUnit> tableModel, final SourceContentsPresenter sourceContentsPresenter, boolean isReadOnly, TargetContentsPresenter targetContentsPresenter)
   {
      this.isReadOnly = isReadOnly;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      setRowRenderer(rowRenderer);
      sourceColumnDefinition.setCellRenderer(sourceCellRenderer);
      targetColumnDefinition.setCellRenderer(targetCellRenderer);

      CancelCallback<TransUnit> cancelCallBack = new CancelCallback<TransUnit>()
      {
         @Override
         public void onCancel(TransUnit cellValue)
         {
            tableModel.onCancel(cellValue);
         }
      };
      EditRowCallback transValueCallBack = new EditRowCallback()
      {
         @Override
         public void gotoNextRow()
         {
            tableModel.gotoNextRow();
         }

         @Override
         public void gotoPrevRow()
         {
            tableModel.gotoPrevRow();
         }

         @Override
         public void gotoFirstRow()
         {
            tableModel.gotoFirstRow();
         }

         @Override
         public void gotoLastRow()
         {
            tableModel.gotoLastRow();
         }

         @Override
         public void gotoNextFuzzyNewRow()
         {
            tableModel.gotoNextFuzzyNew();
         }

         @Override
         public void gotoPrevFuzzyNewRow()
         {
            tableModel.gotoPrevFuzzyNew();
         }

         @Override
         public void gotoNextFuzzyRow()
         {
            tableModel.gotoNextFuzzy();
         }

         @Override
         public void gotoPrevFuzzyRow()
         {
            tableModel.gotoPrevFuzzy();
         }

         @Override
         public void gotoNextNewRow()
         {
            tableModel.gotoNextNew();
         }

         @Override
         public void gotoCurrentRow(boolean andEdit)
         {
            tableModel.gotoCurrentRow(andEdit);
         }

         @Override
         public void gotoPrevNewRow()
         {
            tableModel.gotoPrevNew();
         }

         public void setRowValueOverride(int row, TransUnit targetCell)
         {
            tableModel.setRowValueOverride(row, targetCell);
         }
      };
      this.targetCellEditor = new InlineTargetCellEditor(cancelCallBack, transValueCallBack, isReadOnly, targetContentsPresenter);
//      this.transUnitDetailsContent = new TransUnitDetailsPanel(messages.transUnitDetailsHeading());
      targetColumnDefinition.setCellEditor(targetCellEditor);

      addColumnDefinition(sourceColumnDefinition);
      addColumnDefinition(targetColumnDefinition);
   }

   public InlineTargetCellEditor getTargetCellEditor()
   {
      return targetCellEditor;
   }

   public void setTransUnitDetails(TransUnit selectedTransUnit)
   {
      if (selectedTransUnit != null && sourcePanelMap.get(selectedTransUnit.getId()) != null)
      {
         VerticalPanel sourcePanel = sourcePanelMap.get(selectedTransUnit.getId());
         FlowPanel wrapper = new FlowPanel();
         wrapper.addStyleName("TransUnitDetail-Wrapper");

         transUnitDetailsContent.setDetails(selectedTransUnit);
         wrapper.add(transUnitDetailsContent);
         sourcePanel.add(wrapper);
         sourcePanel.setCellVerticalAlignment(transUnitDetailsContent, HasVerticalAlignment.ALIGN_BOTTOM);
      }
   }
}
