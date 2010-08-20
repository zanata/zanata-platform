package net.openl10n.flies.webtrans.client.editor;

import net.openl10n.flies.webtrans.client.ui.ExpandingTextArea;
import net.openl10n.flies.webtrans.shared.model.TransUnit;

import org.gwt.mosaic.override.client.HTMLTable;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitRowEditor implements HasKeyUpHandlers
{

   public static interface Callback
   {
      void onSave(RowEditInfo rowEditInfo, TransUnit rowValue);

      void onCancel(RowEditInfo rowEditInfo);
   }

   public static class RowEditInfo
   {
      private final int index;
      private final HTMLTable table;

      public RowEditInfo(HTMLTable table, int index)
      {
         this.index = index;
         this.table = table;
      }

      public int getIndex()
      {
         return index;
      }

      public HTMLTable getTable()
      {
         return table;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == null)
            return false;
         if (obj == this)
            return true;
         if (!(obj instanceof RowEditInfo))
            return false;
         RowEditInfo other = (RowEditInfo) obj;
         return this.table == other.table && index == other.index;
      }
   }

   /**
    * Default style name.
    */
   public static final String DEFAULT_STYLENAME = "gwt-TargetCellEditor";

   private boolean dirty;

   private Callback curCallback = null;

   private Widget cellViewWidget;
   private RowEditInfo curCellEditInfo = null;

   private TransUnit cellValue;

   private final ExpandingTextArea textArea;

   /*
    * The minimum height of the target editor
    */
   private static final int MIN_HEIGHT = 48;

   /**
    * Construct a new {@link TransUnitRowEditor} with the specified images.
    * 
    * @param content the {@link Widget} used to edit
    * @param images the images to use for the accept/cancel buttons
    */
   @Inject
   public TransUnitRowEditor(final NavigationMessages messages)
   {
      textArea = new ExpandingTextArea();
      textArea.setStyleName("TableEditorContent-Edit");
      textArea.addChangeHandler(new ChangeHandler()
      {
         @Override
         public void onChange(ChangeEvent event)
         {
            checkDirtyState();
         }
      });
      textArea.addKeyUpHandler(new KeyUpHandler()
      {
         @Override
         public void onKeyUp(KeyUpEvent event)
         {
            checkDirtyState();
         }
      });
   }

   private void checkDirtyState()
   {
      if (!isActive())
      {
         dirty = false;
         return;
      }

      dirty = !textArea.getText().equals(cellValue.getTarget());
   }

   public boolean isDirty()
   {
      return dirty;
   }

   public boolean isActive()
   {
      return cellValue != null;
   }

   public void editCell(RowEditInfo cellEditInfo, TransUnit cellValue, Callback callback)
   {

      if (isActive())
      {
         if (cellEditInfo.equals(curCellEditInfo))
         {
            return;
         }
         release();
      }

      Log.debug("starting edit of cell");

      // Save the current values
      curCallback = callback;
      curCellEditInfo = cellEditInfo;

      HTMLTable table = curCellEditInfo.getTable();
      int curRow = curCellEditInfo.getIndex();

      cellViewWidget = table.getWidget(curRow, ListEditorTableDefinition.TARGET_COL);

      int height = curCellEditInfo.getTable().getWidget(curRow, ListEditorTableDefinition.SOURCE_COL).getOffsetHeight() - 10;

      //int realHeight = height > MIN_HEIGHT ? height : MIN_HEIGHT;
      textArea.setHeight(height + "px");

      int width = table.getWidget(curRow, ListEditorTableDefinition.SOURCE_COL).getOffsetWidth() - 10;
      textArea.setWidth(width + "px");

      table.setWidget(curRow, ListEditorTableDefinition.TARGET_COL, textArea);
      textArea.setText(cellValue.getTarget());

      this.cellValue = TransUnit.copy(cellValue);

      textArea.setFocus(true);
      DOM.scrollIntoView(textArea.getElement());
   }

   public void release()
   {
      if (!isActive())
         return;

      if (!isDirty())
      {
         cancelEdit();
      }
      else
      {
         cellValue.setTarget(textArea.getText());

         // Send the new cell value to the callback
         RowEditInfo cellEditInfo = curCellEditInfo;
         TransUnit cValue = cellValue;
         Callback callback = curCallback;
         detach();

         callback.onSave(cellEditInfo, cValue);
      }
   }

   private void detach()
   {
      if (curCellEditInfo != null && cellViewWidget != null)
      {
         curCellEditInfo.getTable().setWidget(curCellEditInfo.getIndex(), ListEditorTableDefinition.TARGET_COL, cellViewWidget);
         cellViewWidget.getParent().setHeight(cellViewWidget.getOffsetHeight() + "px");
      }

      curCallback = null;
      curCellEditInfo = null;
      cellViewWidget = null;
      cellValue = null;
      dirty = false;
   }

   /**
    * Cancel the cell edit.
    */
   public void cancelEdit()
   {
      RowEditInfo cellEditInfo = curCellEditInfo;
      Callback callback = curCallback;
      detach();
      callback.onCancel(cellEditInfo);
   }

   @Override
   public void fireEvent(GwtEvent<?> event)
   {
      textArea.fireEvent(event);
   }

   @Override
   public HandlerRegistration addKeyUpHandler(KeyUpHandler handler)
   {
      return textArea.addKeyUpHandler(handler);
   }

}
