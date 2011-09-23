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
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
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
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
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
    * acceptHandler.onClick(null); Log.info(
    * "InlineTargetCellEditor.java: Clone-and-save action (The last clone action is called by this action)."
    * ); } };
    *****************/

   /**
    * The click listener used to save as fuzzy.
    */
   private ClickHandler fuzzyHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         acceptFuzzyEdit();
      }
   };

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

   /**
    * The click listener used to accept.
    */
   private ClickHandler acceptHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         saveApprovedAndMoveRow(NavigationType.NextEntry);
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
   private FlowPanel layoutTable;

   private Widget cellViewWidget;

   private TransUnit cellValue;

   private EditorTextArea textArea;
   private HorizontalPanel operationsPanel;

   private boolean isFocused = false;
   private boolean isOpened = false;

   private int curRow;
   private int curCol;
   private HTMLTable table;

   /*
    * The minimum height of the target editor
    */
   // private static final int MIN_HEIGHT = 48;

   /**
    * Construct a new {@link InlineTargetCellEditor} with the specified images.
    */
   public InlineTargetCellEditor(final NavigationMessages messages, CancelCallback<TransUnit> callback, EditRowCallback rowCallback, final EventBus eventBus)
   {
      // Wrap contents in a table
      layoutTable = new FlowPanel();
      layoutTable.setWidth("100%");
      // layoutTable.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

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
            autoSize();
         }

      });
      textArea.addBlurHandler(new BlurHandler()
      {
         @Override
         public void onBlur(BlurEvent event)
         {
            Log.debug("text area focus lost");
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

      textArea.addKeyDownHandler(new KeyDownHandler()
      {

         @Override
         public void onKeyDown(KeyDownEvent event)
         {
            int keyCode = event.getNativeKeyCode();
            // using keydown for Ctrl+S in order to override browser Ctrl+S on
            // keydown (chrome)
            if (event.isControlKeyDown() && keyCode == TableConstants.KEY_S)
            {
               event.stopPropagation();
               event.preventDefault(); // stop browser save
               acceptFuzzyEdit();
            }
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
               saveApprovedAndMoveRow(NavigationType.NextEntry);
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
            else if (event.isAltKeyDown() && keyCode == TableConstants.KEY_G)
            {
               Log.info("InlineTargetCellEditor.java: Clone action.");
               textArea.setValue(cellValue.getSource(), true);
               textArea.setFocus(true);
            }
            else if (event.isAltKeyDown() && (event.isDownArrow() || keyCode == TableConstants.KEY_K))
            {
               // alt-down
               // See editCell() for saving event
               gotoRow(NavigationType.NextEntry);
            }
            else if (event.isAltKeyDown() && (event.isUpArrow() || keyCode == TableConstants.KEY_J))
            {
               // alt-up
               // See editCell() for saving event
               gotoRow(NavigationType.PrevEntry);
            }
            else if (event.isAltKeyDown() && keyCode == KeyCodes.KEY_PAGEDOWN)
            {
               // alt-pagedown
               saveAndMoveNextFuzzy(NavigationType.NextEntry);
            }
            else if (event.isAltKeyDown() && keyCode == KeyCodes.KEY_PAGEUP)
            {
               // alt-pageup
               saveAndMoveNextFuzzy(NavigationType.PrevEntry);
            }
            else if (!event.isAltKeyDown() && !event.isControlKeyDown())
            {
               autoSize();
            }

            // these shortcuts disabled because they conflict with basic text
            // editing:
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

      operationsPanel = new HorizontalPanel();

      operationsPanel.addStyleName("float-right-div");
      operationsPanel.setSpacing(4);
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

      Image fuzzyButton = new Image(images.cellEditorFuzzy());
      fuzzyButton.setStyleName("gwt-Button");
      fuzzyButton.setTitle(messages.fuzzy());
      fuzzyButton.addClickHandler(fuzzyHandler);

      operationsPanel.add(saveButton);
      operationsPanel.add(fuzzyButton);
      operationsPanel.add(cancelButton);
      layoutTable.add(operationsPanel);
   }

   public void gotoRow(NavigationType nav)
   {
      if (nav == NavigationType.NextEntry)
      {
         editRowCallback.gotoNextRow(curRow);
      }
      else if (nav == NavigationType.PrevEntry)
      {
         editRowCallback.gotoPrevRow(curRow);
      }
   }

   private void gotoFuzzyRow(NavigationType nav)
   {
      if (nav == NavigationType.NextEntry)
      {
         editRowCallback.gotoNextFuzzy(curRow);
      }
      else if (nav == NavigationType.PrevEntry)
      {
         editRowCallback.gotoPrevFuzzy(curRow);
      }
   }

   private void restoreView()
   {
      if (curCellEditInfo != null && cellViewWidget != null)
      {
         curCellEditInfo.getTable().setWidget(curRow, curCol, cellViewWidget);
         cellViewWidget.getParent().setHeight(cellViewWidget.getOffsetHeight() + "px");
      }
   }

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
         cellValue.setTarget("");
         textArea.setText(text);
      }
   }

   @Override
   public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue, Callback<TransUnit> callback)
   {
      if (isEditing())
      {
         if (cellEditInfo.getCellIndex() == curCol && cellEditInfo.getRowIndex() == curRow)
         {
            return;
         }
         restoreView();
      }

      // save the content in previous cell before start new editing
      if (curRow != cellEditInfo.getRowIndex())
      {
         savePendingChange(false);
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

      // layoutTable.setCellWidth(this.operationsPanel, "20px");
      table.setWidget(curRow, curCol, layoutTable);
      textArea.setText(cellValue.getTarget());

      autoSize();

      this.cellValue = cellValue;
      textArea.setFocus(true);
      isOpened = true;
      DOM.scrollIntoView(table.getCellFormatter().getElement(curRow, curCol));
   }

   public void savePendingChange(boolean cancelIfUnchanged)
   {
      // if something has changed, save as approved
      if (isEditing() && !cellValue.getTarget().equals(textArea.getText()))
      {
         Log.info("savePendingChange - acceptEdit");
         cellValue.setStatus(ContentState.Approved);
         acceptEdit();
      }
      else
      {
         if (cancelIfUnchanged)
         {
            Log.info("savePendingChange- cancel edit");
            cancelEdit();
         }
      }
   }

   public void saveAndMoveNextFuzzy(NavigationType nav)
   {
      savePendingChange(true);
      gotoFuzzyRow(nav);
   }

   /**
    * save the contents of the cell as approved and move to the next row.
    */
   private void saveApprovedAndMoveRow(NavigationType nav)
   {
      cellValue.setStatus(ContentState.Approved);
      acceptEdit();
      gotoRow(nav);
   }

   /**
    * Accept the contents of the cell editor as the new cell value.
    */
   public void acceptEdit()
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
      textArea.setFocus(false);
      isOpened = false;

      // Send the new cell value to the callback
      curCallback.onComplete(curCellEditInfo, cellValue);
      clearSelection();
   }

   /**
    * Save the contents of the cell and set status to fuzzy.
    */
   protected void acceptFuzzyEdit()
   {
      String text = textArea.getText();
      cellValue.setTarget(text);
      if (text == null || text.isEmpty())
         cellValue.setStatus(ContentState.New);
      else
         cellValue.setStatus(ContentState.NeedReview);
      curCallback.onComplete(curCellEditInfo, cellValue);
   }

   /**
    * Cancel the cell edit.
    */
   public void cancelEdit()
   {
      // Fire the event
      if (!onCancel())
      {
         return;
      }

      restoreView();
      textArea.setFocus(false);
      isOpened = false;

      // Call the callback
      if (curCallback != null)
      {
         // curCallback.onCancel(curCellEditInfo);
         cancelCallback.onCancel(cellValue);
      }

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
   }
}
