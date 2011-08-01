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
import org.zanata.webtrans.client.events.EditTransUnitEvent;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class InlineTargetCellEditor implements CellEditor<TransUnit>
{
   /**
    * Default style name.
    */
   public static final String DEFAULT_STYLENAME = "gwt-TargetCellEditor";

   /**
    * The click listener used to clone.
    */
   /************
    * private ClickHandler cloneHandler = new ClickHandler() { public void
    * onClick(ClickEvent event) { textArea.setText(cellValue.getSource());
    * textArea.setFocus(true); autoSize(); enableSaveButton();
    * Log.info("InlineTargetCellEditor.java: Clone action."); } };
    *************/
   /**
    * The click listener used to clone and save.
    */
   /****************
    * private ClickHandler cloneAndSaveHandler = new ClickHandler() { public
    * void onClick(ClickEvent event) { cloneHandler.onClick(null);
    * acceptHandler.onClick(null); Log.info("InlineTargetCellEditor.java: Clone-and-save action (The last clone action is called by this action)."
    * ); } };
    *****************/

   /**
    * The click listener used to cancel.
    */
   private ClickHandler cancelHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         cancelEdit();
         // disableSaveButton();
      }
   };
   
   // private final CheckBox toggleFuzzy;
   // private Image saveButton;

   /**
    * The click listener used to accept.
    */
   private ClickHandler acceptHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         acceptEdit();
         // gotoNextRow(curRow);
      }
   };

   /**
    * The current {@link CellEditor.Callback}.
    */
   private Callback<TransUnit> curCallback = null;

   private CancelCallback<TransUnit> cancelCallback = null;

   private EditRowCallback editRowCallback = null;

   /**
    * The current {@link CellEditor.CellEditInfo}.
    */
   private CellEditInfo curCellEditInfo = null;

   /**
    * The main grid used for layout.
    */
   private HorizontalPanel layoutTable;

   private Widget cellViewWidget;

   private TransUnit cellValue;

   private EditorTextArea textArea;
   private VerticalPanel operationsPanel;

   private boolean isFocused = false;
   // private boolean allowFuzzyOverride = false;
   private boolean isOpened = false;

   // private Image stateImage;

   private int curRow;
   private int curCol;
   private HTMLTable table;

   /*
    * The minimum height of the target editor
    */
   // private static final int MIN_HEIGHT = 48;

   private static final int KEY_G = 'G';
   private static final int KEY_J = 'J';
   private static final int KEY_K = 'K';

   /**
    * Construct a new {@link InlineTargetCellEditor} with the specified images.
    */
   public InlineTargetCellEditor(final NavigationMessages messages, CancelCallback<TransUnit> callback, EditRowCallback rowCallback, final EventBus eventBus)
   {
      // Wrap contents in a table
      layoutTable = new HorizontalPanel();
      layoutTable.setWidth("100%");

      // this.eventBus = eventBus;
      cancelCallback = callback;
      editRowCallback = rowCallback;
      textArea = new EditorTextArea();
      textArea.setStyleName("TableEditorContent-Edit");
      textArea.addValueChangeHandler(new ValueChangeHandler<String>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            // remove fuzzy mark only at beginning if fuzzy is marked
            // if (allowFuzzyOverride)
            // removeFuzzyMark();
            // enable save button when start typing
            // enableSaveButton();
            // allowFuzzyOverride = false;
            autoSize();
         }

      });
      textArea.addBlurHandler(new BlurHandler()
      {
         @Override
         public void onBlur(BlurEvent event)
         {
            isFocused = false;
         }

      });
      textArea.addFocusHandler(new FocusHandler()
      {

         @Override
         public void onFocus(FocusEvent event)
         {
            isFocused = true;
         }

      });
      textArea.addKeyUpHandler(new KeyUpHandler()
      {
         @Override
         public void onKeyUp(KeyUpEvent event)
         {
            eventBus.fireEvent(new EditTransUnitEvent());
            int keyCode = event.getNativeKeyCode();
            
            // NB: if you change these, please change NavigationConsts too!
            if (event.isControlKeyDown() && keyCode == KeyCodes.KEY_ENTER)
            {
               acceptEdit();
               gotoNextRow(curRow);
            }
            if (keyCode == KeyCodes.KEY_ESCAPE)
            {
               cancelEdit();
            }
            // else if (event.isControlKeyDown() && event.isShiftKeyDown() &&
            // event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
            // { // was alt-e
            // handleNextState(ContentState.NeedReview);
            // }
            // else if (event.isControlKeyDown() && event.isShiftKeyDown() &&
            // event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
            // { // was alt-m
            // handlePrevState(ContentState.NeedReview);
            // // } else if(event.isControlKeyDown() && event.getNativeKeyCode()
            // // == KeyCodes.KEY_PAGEDOWN) { // bad in Firefox
            // }
            else if (event.isAltKeyDown() && keyCode == KEY_G)
            {
               Log.info("InlineTargetCellEditor.java: Clone action.");
               textArea.setValue(cellValue.getSource(), true);
               textArea.setFocus(true);
            }
            else if (event.isAltKeyDown() && (event.isDownArrow() || keyCode == KEY_K))
            {
               handleNext();
               // } else if(event.isControlKeyDown() && event.getNativeKeyCode()
               // == KeyCodes.KEY_PAGEUP) { // bad in Firefox
            }
            else if (event.isAltKeyDown() && (event.isUpArrow() || keyCode == KEY_J))
            {
               handlePrev();
            }
            else if (event.isAltKeyDown() && keyCode == KeyCodes.KEY_PAGEDOWN)
            { // alt-pagedown
               handleNextState();
            }
            else if (event.isAltKeyDown() && keyCode == KeyCodes.KEY_PAGEUP)
            { // alt-pageup
               handlePrevState();
            }
            // else if (event.isAltKeyDown() && keyCode == KEY_N)
            // {
            // if (toggleFuzzy.getValue())
            // toggleFuzzy.setValue(false);
            // else
            // toggleFuzzy.setValue(true);
            // }

            else if (!event.isAltKeyDown() && !event.isControlKeyDown())
            {
               autoSize();
            }

            // these shortcuts disabled because they conflict with basic text editing:
            // else if (event.isControlKeyDown() && event.getNativeKeyCode() ==
            // KeyCodes.KEY_HOME)
            // { // ctrl-home
            // cloneHandler.onClick(null);
            // }
            // else if (event.isControlKeyDown() && event.getNativeKeyCode() ==
            // KeyCodes.KEY_END)
            // { // ctrl-end
            // cloneAndSaveHandler.onClick(null);
            // }
         }

      });

      layoutTable.add(textArea);

      operationsPanel = new VerticalPanel();
      operationsPanel.setHeight("100%");
      operationsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      operationsPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

      // operationsPanel.addStyleName("float-right-div");
      // operationsPanel.setSpacing(4);
      // layoutTable.add(operationsPanel);

      // icon as the current state of the unit
      // stateImage = new Image(resources.newUnit());
      // operationsPanel.add(stateImage);

      // PushButton doesn't allow to have images and text at the same time
      TableResources images = GWT.create(TableResources.class);
      Image cancelButton = new Image(images.cellEditorCancel());
      // cancelButton.setText(messages.editCancel());
      cancelButton.setStyleName("gwt-Button");
      cancelButton.setTitle(messages.editCancelShortcut());
      cancelButton.addClickHandler(cancelHandler);

      Image saveButton = new Image(images.cellEditorAccept());
      // saveButton.setText(messages.editSave());
      saveButton.setStyleName("gwt-Button");
      saveButton.setTitle(messages.editSaveShortcut());
      saveButton.addClickHandler(acceptHandler);

      operationsPanel.add(saveButton);
      operationsPanel.add(cancelButton);
      layoutTable.add(operationsPanel);
   }

   private void gotoNextRow(int row)
   {
      editRowCallback.gotoNextRow(row);
   }

   private void gotoPrevRow(int row)
   {
      editRowCallback.gotoPrevRow(row);
   }

   private void gotoNextFuzzy(int row)
   {
      editRowCallback.gotoNextFuzzy(row);
   }

   private void gotoPrevFuzzy(int row)
   {
      editRowCallback.gotoPrevFuzzy(row);
   }

   private void restoreView()
   {
      if (curCellEditInfo != null && cellViewWidget != null)
      {
         curCellEditInfo.getTable().setWidget(curRow, curCol, cellViewWidget);
         cellViewWidget.getParent().setHeight(cellViewWidget.getOffsetHeight() + "px");
      }
   }

   /*********************
    * private boolean isDirty() { if (cellValue == null) return false; return
    * !textArea.getText().equals(cellValue.getTarget()); }
    ********************/

   public boolean isEditing()
   {
      return cellValue != null;
   }

   public boolean isFocused()
   {
      return isFocused;
   }

   public boolean isOpened()
   {
      return isOpened;
   }

   public void setText(String text)
   {
      if (isEditing())
      {
         textArea.setText(text);
      }
   }

   public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue, Callback<TransUnit> callback)
   {
      // don't allow edits of two cells at once
      // if (isDirty())
      // {
      // callback.onCancel(cellEditInfo);
      // return;
      // }

      if (isEditing())
      {
         if (cellEditInfo.getCellIndex() == curCol && cellEditInfo.getRowIndex() == curRow)
         {
            return;
         }
         restoreView();
      }
      
      // save the content in previous cell before start new editing
      if (this.cellValue != null && curRow != cellEditInfo.getRowIndex() && !textArea.getText().equals(this.cellValue.getTarget()))
      {
         Log.debug("save content of previous cell");
         acceptEdit();
      }

      Log.debug("starting edit of cell");

      // Save the current values
      curCallback = callback;
      curCellEditInfo = cellEditInfo;

      // Get the info about the cell
      table = curCellEditInfo.getTable();

      curRow = curCellEditInfo.getRowIndex();
      curCol = curCellEditInfo.getCellIndex();

      cellViewWidget = table.getWidget(curRow, curCol);

      // int height = table.getWidget(curRow, curCol - 1).getOffsetHeight();

      // int realHeight = height > MIN_HEIGHT ? height : MIN_HEIGHT;

      //Disable it for autosize
      // textArea.setHeight(realHeight + "px");

      // Leave space for operationsPanel
      int width = table.getWidget(curRow, 0).getOffsetWidth() - 60;
      // layoutTable.setHeight(realHeight + "px");
      textArea.setWidth(width + "px");

      table.setWidget(curRow, curCol, layoutTable);
      
      textArea.setText(cellValue.getTarget());

      autoSize();

      int height = textArea.getOffsetHeight();
      operationsPanel.setHeight(height + "px");

      this.cellValue = cellValue;
      textArea.setFocus(true);
      isOpened = true;
      DOM.scrollIntoView(table.getCellFormatter().getElement(curRow, curCol));
      // toggleFuzzy.setValue(cellValue.getStatus() == ContentState.NeedReview);
      // if (cellValue.getStatus() == ContentState.NeedReview)
      // allowFuzzyOverride = true;
      // preStatus = cellValue.getStatus();
      // refreshStateImage();
   }

   // private void refreshStateImage()
   // {
   // if (cellValue.getStatus() == ContentState.New)
   // stateImage.setUrl(resources.newUnit().getURL());
   // else if (cellValue.getStatus() == ContentState.NeedReview)
   // stateImage.setUrl(resources.fuzzyUnit().getURL());
   // else if (cellValue.getStatus() == ContentState.Approved)
   // stateImage.setUrl(resources.approvedUnit().getURL());
   // }

   /**
    * Accept the contents of the cell editor as the new cell value.
    */
   protected void acceptEdit()
   {
      // Check if we are ready to accept
      if (!onAccept())
      {
         return;
      }
      cellValue.setTarget(textArea.getText());

      // changing status to new when target cell is empty
      if (cellValue.getTarget().isEmpty())
         cellValue.setStatus(ContentState.New);
      else if (cellValue.getStatus() == ContentState.New)
         cellValue.setStatus(ContentState.Approved);

      restoreView();
      // disableSaveButton();
      isOpened = false;

      // Send the new cell value to the callback
      curCallback.onComplete(curCellEditInfo, cellValue);
      clearSelection();
   }


   public void clearSelection()
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
      // Fire the event
      if (!onCancel())
      {
         return;
      }

      restoreView();
      isOpened = false;

      // restore to previous status
      // if (cellValue != null)
      // {
      // cellValue.setStatus(preStatus);
      // eventBus.fireEvent(new ToggleFuzzyEvent(cellValue));
      // }

      // Call the callback
      if (curCallback != null)
      {
         // curCallback.onCancel(curCellEditInfo);
         cancelCallback.onCancel(cellValue);
      }

      clearSelection();
   }

   /**
    * Called before an accept takes place.
    * 
    * @return true to allow the accept, false to prevent it
    */
   protected boolean onAccept()
   {
      return true;
   }

   /**
    * Called before a cancel takes place.
    * 
    * @return true to allow the cancel, false to prevent it
    */
   protected boolean onCancel()
   {
      return true;
   }

   public void handleNext()
   {
      gotoNextRow(curRow);
   }

   public void handlePrev()
   {
      gotoPrevRow(curRow);
   }

   public void handleNextState()
   {
      gotoNextFuzzy(curRow);
   }

   public void handlePrevState()
   {
      gotoPrevFuzzy(curRow);
   }

   public void setTextAreaSize()
   {
      autoSize();
   }

   public int getCurrentRow()
   {
      return curRow;
   }

   public void autoSize()
   {
      int initialLines = 3;
      int growByLines = 1;

      Log.debug("autosize TextArea");

      textArea.setVisibleLines(initialLines);

      while (textArea.getElement().getScrollHeight() > textArea.getElement().getClientHeight())
      {
         textArea.setVisibleLines(textArea.getVisibleLines() + growByLines);
      }

      int height = textArea.getOffsetHeight();
      operationsPanel.setHeight(height + "px");
   }

}
