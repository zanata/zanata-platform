package org.fedorahosted.flies.webtrans.client.editor.table;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.gwt.mosaic.override.client.HTMLTable;
import org.gwt.mosaic.ui.client.table.CellEditor;
import org.gwt.mosaic.ui.client.table.InlineCellEditor.InlineCellEditorImages;

import com.allen_sauer.gwt.log.client.Log;
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

public class InlineTargetCellEditor implements CellEditor<TransUnit>
{

   /**
    * An {@link ImageBundle} that provides images for
    * {@link InlineTargetCellEditor}.
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

   /**
    * The current {@link CellEditor.Callback}.
    */
   private Callback<TransUnit> curCallback = null;

   /**
    * The current {@link CellEditor.CellEditInfo}.
    */
   private CellEditInfo curCellEditInfo = null;

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

   /**
    * Construct a new {@link InlineTargetCellEditor} with the specified images.
    * 
    * @param content the {@link Widget} used to edit
    * @param images the images to use for the accept/cancel buttons
    */
   @Inject
   public InlineTargetCellEditor(final NavigationMessages messages, TargetCellEditorImages images)
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
      layoutTable.add(textArea);

      HorizontalPanel operationsPanel = new HorizontalPanel();
      operationsPanel.addStyleName("float-right-div");
      operationsPanel.setSpacing(4);
      layoutTable.add(operationsPanel);

      // Add content widget
      toggleFuzzy = new CheckBox(messages.fuzzy());
      operationsPanel.add(toggleFuzzy);

      PushButton cancelButton = new PushButton(new Image(images.cellEditorCancel()), cancelHandler);
      cancelButton.setText(messages.editCancel());
      cancelButton.setTitle(messages.editCancelShortcut());
      operationsPanel.add(cancelButton);

      PushButton acceptButton = new PushButton(new Image(images.cellEditorAccept()), acceptHandler);
      acceptButton.setText(messages.editSave());
      acceptButton.setTitle(messages.editSaveShortcut());
      operationsPanel.add(acceptButton);
   }

   private void restoreView()
   {
      if (curCellEditInfo != null && cellViewWidget != null)
      {
         curCellEditInfo.getTable().setWidget(curCellEditInfo.getRowIndex(), curCellEditInfo.getCellIndex(), cellViewWidget);
         cellViewWidget.getParent().setHeight(cellViewWidget.getOffsetHeight() + "px");
      }
   }

   private boolean isActive()
   {
      return cellValue != null;
   }

   private boolean rowEquals(CellEditInfo r1, CellEditInfo r2)
   {
      return r1.getRowIndex() == r2.getRowIndex();
   }

   public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue, Callback<TransUnit> callback)
   {

      if (isActive())
      {
         if (rowEquals(cellEditInfo, curCellEditInfo))
         {
            return;
         }
         callback.onCancel(cellEditInfo);
         restoreView();
      }

      Log.debug("starting edit of cell");

      // Save the current values
      curCallback = callback;
      curCellEditInfo = cellEditInfo;

      HTMLTable table = curCellEditInfo.getTable();
      int curRow = curCellEditInfo.getRowIndex();
      int curCol = curCellEditInfo.getCellIndex();

      cellViewWidget = table.getWidget(curRow, curCol);

      int height = curCellEditInfo.getTable().getWidget(curRow, curCol - 1).getOffsetHeight();

      int realHeight = height > MIN_HEIGHT ? height : MIN_HEIGHT;

      textArea.setHeight(realHeight + "px");

      int width = table.getWidget(curRow, curCol - 1).getOffsetWidth() - 10;
      textArea.setWidth(width + "px");

      table.setWidget(curRow, curCol, layoutTable);
      textArea.setText(cellValue.getTarget());
      toggleFuzzy.setValue(cellValue.getStatus() == ContentState.NeedReview);

      this.cellValue = cellValue; // TODO copy

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
      restoreView();

      // Send the new cell value to the callback
      curCallback.onComplete(curCellEditInfo, cellValue);
      clearSelection();
   }

   private void clearSelection()
   {
      curCallback = null;
      curCellEditInfo = null;
      cellViewWidget = null;
      cellValue = null;
   }

   /**
    * Cancel the cell edit.
    */
   protected void cancelEdit()
   {
      restoreView();
      clearSelection();
   }

}
