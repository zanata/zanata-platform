package org.zanata.webtrans.client.editor.table;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.OpenEditorEvent;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.gen2.table.client.AbstractColumnDefinition;
import com.google.gwt.gen2.table.client.CellRenderer;
import com.google.gwt.gen2.table.client.ColumnDefinition;
import com.google.gwt.gen2.table.client.DefaultTableDefinition;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.VerticalPanel;

public class TargetEditorTableDefinition extends DefaultTableDefinition<TransUnit>
{
   private final EventBus eventBus;
   private final boolean isReadOnly;
   private final NavigationMessages messages;

   private String findMessage;
   private InlineTargetCellEditor targetCellEditor;

   public void setFindMessage(String findMessage)
   {
      this.findMessage = findMessage;
   }

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
         targetPanel.addStyleName("TableEditorCell-Target-Table");
         final HighlightingLabel label = new HighlightingLabel();

         /**
          * if editor is opening, do not render target cell, otherwise editor
          * will be closed. targetCellEditor.isEditing not suitable since when
          * we click the save button, cellValue is not null.
          **/
         if (targetCellEditor.isOpened() && targetCellEditor.getTargetCell().getId().equals(rowValue.getId()))
         {
            return;
         }

         if (rowValue.getTargets().isEmpty() && !isReadOnly)
         {
            label.setText(messages.clickHere());
            label.setStylePrimaryName("TableEditorContent-Empty");
         }
         else
         {
            label.setText(rowValue.getTargets().get(view.getRowIndex()));
            label.setStylePrimaryName("TableEditorContent");
         }

         if (findMessage != null && !findMessage.isEmpty())
         {
            label.highlightSearch(findMessage);
         }
         label.setTitle(messages.clickHere());

         label.sinkEvents(Event.ONMOUSEDOWN);
         final int rowIndex = view.getRowIndex();
         label.addMouseDownHandler(new MouseDownHandler()
         {
            @Override
            public void onMouseDown(MouseDownEvent event)
            {
               if (!isReadOnly && event.getNativeButton() == NativeEvent.BUTTON_LEFT)
               {
                  event.stopPropagation();
                  event.preventDefault();
                  eventBus.fireEvent(new OpenEditorEvent(rowIndex));
               }
            }
         });
         targetPanel.add(label);
         targetPanel.setWidth("100%");
         view.setWidget(targetPanel);
      }
   };

   public TargetEditorTableDefinition(final NavigationMessages messages, EventBus eventBus, final RedirectingCachedTableModel<TransUnit> tableModel, boolean isReadOnly)
   {
      this.eventBus = eventBus;
      this.isReadOnly = isReadOnly;
      this.messages = messages;

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
         public void gotoPrevNewRow()
         {
            tableModel.gotoPrevNew();
         }
      };

      targetColumnDefinition.setCellRenderer(targetCellRenderer);
      this.targetCellEditor = new InlineTargetCellEditor(messages, cancelCallBack, transValueCallBack, eventBus, isReadOnly);
      targetColumnDefinition.setCellEditor(targetCellEditor);
      
      addColumnDefinition(targetColumnDefinition);
   }
}
