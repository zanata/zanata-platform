package org.fedorahosted.flies.webtrans.client.editor;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.gwt.mosaic.override.client.HTMLTable;
import org.gwt.mosaic.ui.client.table.InlineCellEditor.InlineCellEditorImages;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitRowEditor
{

   public static interface Callback
   {
      void onComplete(RowEditInfo rowEditInfo, TransUnit rowValue);

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
         if(obj == null) return false;
         if(obj == this) return true;
         if(! (obj instanceof RowEditInfo) ) return false;
         RowEditInfo other = (RowEditInfo) obj;
         return this.table == other.table && index == other.index;
      }
   }

   /**
    * An {@link ImageBundle} that provides images for {@link TransUnitRowEditor}
    * .
    */
   public static interface TargetCellEditorImages extends InlineCellEditorImages
   {

   }

   /**
    * Default style name.
    */
   public static final String DEFAULT_STYLENAME = "gwt-TargetCellEditor";

   /**
    * The click listener used to cancel.
    */
   private ClickHandler cancelHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         cancelEdit();
      }
   };

   private final CheckBox toggleFuzzy;

   /**
    * The click listener used to accept.
    */
   private ClickHandler acceptHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         acceptEdit();
      }
   };

   private boolean dirty;

   /**
    * The current {@link CellEditor.Callback}.
    */
   private Callback curCallback = null;

   /**
    * The current {@link CellEditor.CellEditInfo}.
    */
   private RowEditInfo curCellEditInfo = null;

   /**
    * The main grid used for layout.
    */
   private FlowPanel layoutTable;

   private Widget cellViewWidget;

   private TransUnit cellValue;

   private final TextArea textArea;

   /*
    * The minimum height of the target editor
    */
   private static final int MIN_HEIGHT = 48;

   private final PushButton cancelButton;
   private final PushButton acceptButton;

   /**
    * Construct a new {@link TransUnitRowEditor} with the specified images.
    * 
    * @param content the {@link Widget} used to edit
    * @param images the images to use for the accept/cancel buttons
    */
   @Inject
   public TransUnitRowEditor(final NavigationMessages messages, TargetCellEditorImages images)
   {
      // Wrap contents in a table
      layoutTable = new FlowPanel();

      textArea = new TextArea();
      textArea.setStyleName("TableEditorContent-Edit");
      textArea.addKeyUpHandler(new KeyUpHandler()
      {
         @Override
         public void onKeyUp(KeyUpEvent event)
         {
            // NB: if you change these, please change NavigationConsts too!
            if (event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
            {
               acceptEdit();
            }
            else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
            {
               cancelEdit();
            }
            else if (event.isControlKeyDown() && event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
            { // was alt-e
              // handleNextState(ContentState.NeedReview);
            }
            else if (event.isControlKeyDown() && event.isShiftKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
            { // was alt-m
              // handlePrevState(ContentState.NeedReview);
              // } else if(event.isControlKeyDown() && event.getNativeKeyCode()
              // == KeyCodes.KEY_PAGEDOWN) { // bad in Firefox
            }
            else if (event.isAltKeyDown() && event.isDownArrow())
            {
               // handleNext();
               // } else if(event.isControlKeyDown() && event.getNativeKeyCode()
               // == KeyCodes.KEY_PAGEUP) { // bad in Firefox
            }
            else if (event.isAltKeyDown() && event.isUpArrow())
            {
               // handlePrev();
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
            { // alt-down
              // handleNextState(ContentState.New);
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
            { // alt-up
              // handlePrevState(ContentState.New);
            }
         }

      });

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

      layoutTable.add(textArea);

      HorizontalPanel operationsPanel = new HorizontalPanel();
      operationsPanel.addStyleName("float-right-div");
      operationsPanel.setSpacing(4);
      layoutTable.add(operationsPanel);

      // Add content widget
      toggleFuzzy = new CheckBox(messages.fuzzy());
      operationsPanel.add(toggleFuzzy);

      cancelButton = new PushButton(new Image(images.cellEditorCancel()), cancelHandler);
      cancelButton.setText(messages.editCancel());
      cancelButton.setTitle(messages.editCancelShortcut());
      operationsPanel.add(cancelButton);

      acceptButton = new PushButton(new Image(images.cellEditorAccept()), acceptHandler);
      acceptButton.setText(messages.editSave());
      acceptButton.setTitle(messages.editSaveShortcut());
      operationsPanel.add(acceptButton);
   }

   private void checkDirtyState()
   {
      dirty = !textArea.getText().equals(cellValue.getTarget());
      acceptButton.setEnabled(dirty);
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
         if ( cellEditInfo.equals(curCellEditInfo) )
         {
            return;
         }
         else if (isDirty())
         {
            acceptEdit();
         }
         else
         {
            cancelEdit();
         }

      }

      Log.debug("starting edit of cell");

      // Save the current values
      curCallback = callback;
      curCellEditInfo = cellEditInfo;

      HTMLTable table = curCellEditInfo.getTable();
      int curRow = curCellEditInfo.getIndex();

      cellViewWidget = table.getWidget(curRow, ListEditorTableDefinition.TARGET_COL);

      int height = curCellEditInfo.getTable().getWidget(curRow, ListEditorTableDefinition.SOURCE_COL).getOffsetHeight();

      int realHeight = height > MIN_HEIGHT ? height : MIN_HEIGHT;

      textArea.setHeight(realHeight + "px");

      int width = table.getWidget(curRow, ListEditorTableDefinition.SOURCE_COL).getOffsetWidth() - 10;
      textArea.setWidth(width + "px");

      table.setWidget(curRow, ListEditorTableDefinition.TARGET_COL, layoutTable);
      textArea.setText(cellValue.getTarget());
      toggleFuzzy.setValue(cellValue.getStatus() == ContentState.NeedReview);

      this.cellValue = TransUnit.copy(cellValue);

      textArea.setFocus(true);
      DOM.scrollIntoView(textArea.getElement());
   }

   /**
    * Accept the contents of the cell editor as the new cell value.
    */
   protected void acceptEdit()
   {
      cellValue.setTarget(textArea.getText());
      if (cellValue.getTarget().isEmpty())
         cellValue.setStatus(ContentState.New);
      else
         cellValue.setStatus(toggleFuzzy.getValue() ? ContentState.NeedReview : ContentState.Approved);

      // Send the new cell value to the callback
      RowEditInfo cellEditInfo = curCellEditInfo;
      TransUnit cValue = cellValue;
      Callback callback = curCallback;
      detach();

      callback.onComplete(cellEditInfo, cValue);

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
      acceptButton.setEnabled(false);
   }

   /**
    * Cancel the cell edit.
    */
   protected void cancelEdit()
   {
      RowEditInfo cellEditInfo = curCellEditInfo;
      Callback callback = curCallback;
      detach();
      callback.onCancel(cellEditInfo);
   }

}
