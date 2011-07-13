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

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.ToggleFuzzyEvent;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.gen2.table.client.RowRenderer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TableEditorTableDefinition extends DefaultTableDefinition<TransUnit>
{

   // public static final int INDICATOR_COL = 0;
   public static final int SOURCE_COL = 0;
   public static final int TARGET_COL = 1;

   private String findMessage;
   private CheckBox toggleFuzzy;
   private EventBus eventBus;

   private final RowRenderer<TransUnit> rowRenderer = new RowRenderer<TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, AbstractRowView<TransUnit> view)
      {
         String styles = "TableEditorRow ";
         styles += view.getRowIndex() % 2 == 0 ? "odd-row" : "even-row";


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

   // private final AbstractColumnDefinition<TransUnit, TransUnit>
   // indicatorColumnDefinition =
   // new AbstractColumnDefinition<TransUnit, TransUnit>() {
   // @Override
   // public TransUnit getCellValue(TransUnit rowValue) {
   // return rowValue;
   // }
   //
   // @Override
   // public void setCellValue(TransUnit rowValue, TransUnit cellValue) {
   // cellValue.setSource(rowValue.getSource());
   // }
   // };
   //
   // private final CellRenderer<TransUnit, TransUnit> indicatorCellRenderer =
   // new CellRenderer<TransUnit, TransUnit>() {
   // @Override
   // public void renderRowValue(
   // TransUnit rowValue,
   // ColumnDefinition<TransUnit, TransUnit> columnDef,
   // com.google.gwt.gen2.table.client.TableDefinition.AbstractCellView<TransUnit>
   // view) {
   // view.setStyleName("TableEditorCell TableEditorCell-Source");
   // if(rowValue.getEditStatus().equals(EditState.Lock)) {
   // Image image = new Image("../img/silk/user.png");
   // view.setWidget(image);
   // }
   // }
   // };

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
         cellValue.setSource(rowValue.getSource());
         cellValue.setSourceComment(rowValue.getSourceComment());
      }
   };

   private final CellRenderer<TransUnit, TransUnit> sourceCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, com.google.gwt.gen2.table.client.TableDefinition.AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Source");
         SourcePanel sourcePanel = new SourcePanel(rowValue, messages);
         if (findMessage != null && !findMessage.isEmpty())
         {
            sourcePanel.highlightSearch(findMessage);
         }
         view.setWidget(sourcePanel);
      }
   };

   private final AbstractColumnDefinition<TransUnit, TransUnit> operationsColumnDefinition = new AbstractColumnDefinition<TransUnit, TransUnit>()
   {
      @Override
      public TransUnit getCellValue(TransUnit rowValue)
      {
         return rowValue;
      }

      @Override
      public void setCellValue(TransUnit rowValue, TransUnit cellValue)
      {
         cellValue.setStatus(rowValue.getStatus());
      }
   };

   private final CellRenderer<TransUnit, TransUnit> operationsCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(final TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, AbstractCellView<TransUnit> view)
      {
         // view.setStyleName("TableEditorCell TableEditorCell-Source");
         VerticalPanel operationsPanel = new VerticalPanel();
         toggleFuzzy = new CheckBox(messages.fuzzy());
         if (rowValue.getStatus() == ContentState.NeedReview)
            toggleFuzzy.setValue(true);
         else
            toggleFuzzy.setValue(false);
         toggleFuzzy.addValueChangeHandler(new ValueChangeHandler<Boolean>()
         {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event)
            {
               if (event.getValue())
                  rowValue.setStatus(ContentState.NeedReview);
               else
                  rowValue.setStatus(ContentState.New);

               eventBus.fireEvent(new ToggleFuzzyEvent(rowValue));
            }

         });
         operationsPanel.add(toggleFuzzy);
         view.setWidget(operationsPanel);
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
         cellValue.setTarget(rowValue.getTarget());
      }

   };

   private final CellRenderer<TransUnit, TransUnit> targetCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Target");

         final Label label = new HighlightingLabel();

         // if editor is opening, do not render target cell, otherwise editor
         // will be closed
         if (targetCellEditor.isEditing())
            return;

         if (rowValue.getTarget().isEmpty())
         {
            label.setText(messages.clickHere());
            label.setStylePrimaryName("TableEditorContent-Empty");
         }
         else
         {
            label.setText(rowValue.getTarget());
            label.setStylePrimaryName("TableEditorContent");
         }

         if (findMessage != null && !findMessage.isEmpty())
         {
            ((HighlightingLabel) label).highlightSearch(findMessage);
         }

         label.setTitle(messages.clickHere());

         // TODO label.setTitle(rowValue.getTargetComment());
         view.setWidget(label);
      }
   };

   private InlineTargetCellEditor targetCellEditor;
   private final NavigationMessages messages;

   public void setFindMessage(String findMessage)
   {
      Log.info("set find message: " + findMessage);
      this.findMessage = findMessage;
   }

   public TableEditorTableDefinition(final NavigationMessages messages, final RedirectingCachedTableModel<TransUnit> tableModel, final EventBus eventBus)
   {
      this.messages = messages;
      this.eventBus = eventBus;
      setRowRenderer(rowRenderer);
      // indicatorColumnDefinition.setMaximumColumnWidth(15);
      // indicatorColumnDefinition.setPreferredColumnWidth(15);
      // indicatorColumnDefinition.setMinimumColumnWidth(15);
      // indicatorColumnDefinition.setCellRenderer(indicatorCellRenderer);
      sourceColumnDefinition.setCellRenderer(sourceCellRenderer);
      operationsColumnDefinition.setMaximumColumnWidth(60);
      operationsColumnDefinition.setPreferredColumnWidth(60);
      operationsColumnDefinition.setMinimumColumnWidth(60);
      operationsColumnDefinition.setCellRenderer(operationsCellRenderer);
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
         public void gotoNextRow(int row)
         {
            tableModel.gotoNextRow(row);
         }

         @Override
         public void gotoPrevRow(int row)
         {
            tableModel.gotoPrevRow(row);
         }

         @Override
         public void gotoNextFuzzy(int row)
         {
            tableModel.gotoNextFuzzy(row);
         }

         @Override
         public void gotoPrevFuzzy(int row)
         {
            tableModel.gotoPrevFuzzy(row);
         }
      };
      
      this.targetCellEditor = new InlineTargetCellEditor(messages, cancelCallBack, transValueCallBack, eventBus);
      targetColumnDefinition.setCellEditor(targetCellEditor);
      // See _INDEX consts above if modifying!
      // addColumnDefinition(indicatorColumnDefinition);
      addColumnDefinition(sourceColumnDefinition);
      addColumnDefinition(operationsColumnDefinition);
      addColumnDefinition(targetColumnDefinition);
   }

   public InlineTargetCellEditor getTargetCellEditor()
   {
      return targetCellEditor;
   }

}
