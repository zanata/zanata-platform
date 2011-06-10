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
import net.customware.gwt.presenter.client.EventBus;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.client.InlineCellEditor.InlineCellEditorImages;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

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
    * The click listener used to clone.
    */
   private ClickHandler cloneHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         textArea.setText(cellValue.getSource());
         textArea.setFocus(true);
         Log.info("InlineTargetCellEditor.java: Clone action.");
      }
   };

   /**
    * The click listener used to clone and save.
    */
   private ClickHandler cloneAndSaveHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         cloneHandler.onClick(null);
         acceptHandler.onClick(null);
         Log.info("InlineTargetCellEditor.java: Clone-and-save action (The last clone action is called by this action).");
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

   private final CheckBox toggleFuzzy;

   /**
    * The click listener used to accept.
    */
   private ClickHandler acceptHandler = new ClickHandler()
   {
      public void onClick(ClickEvent event)
      {
         acceptEdit();
         gotoNextRow(curRow);
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

   private final TextArea textArea;

   private boolean isFocused = false;

   // private Image stateImage;

   private int curRow;
   private int curCol;
   private HTMLTable table;


   /*
    * The minimum height of the target editor
    */
   private static final int MIN_HEIGHT = 48;

   /**
    * Construct a new {@link InlineTargetCellEditor}.
    * 
    * @param content the {@link Widget} used to edit
    */
   public InlineTargetCellEditor(final NavigationMessages messages, CancelCallback<TransUnit> callback, EditRowCallback tranValueCallback, final EventBus eventBus)
   {
      this(messages, GWT.<TargetCellEditorImages> create(TargetCellEditorImages.class), callback, tranValueCallback, eventBus);
   }

   /**
    * Construct a new {@link InlineTargetCellEditor} with the specified images.
    * 
    * @param content the {@link Widget} used to edit
    * @param images the images to use for the accept/cancel buttons
    */
   public InlineTargetCellEditor(final NavigationMessages messages, TargetCellEditorImages images, CancelCallback<TransUnit> callback, EditRowCallback rowCallback, final EventBus eventBus)
   {
      // Wrap contents in a table
      layoutTable = new FlowPanel();

      cancelCallback = callback;
      editRowCallback = rowCallback;
      textArea = new TextArea();
      textArea.setStyleName("TableEditorContent-Edit");
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
            // NB: if you change these, please change NavigationConsts too!
            if (event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
            {
               acceptEdit();
               gotoNextRow(curRow);
            }
            else if (event.getNativeKeyCode() == KeyCodes.KEY_ESCAPE)
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
            else if (event.isAltKeyDown() && event.isDownArrow())
            {
               handleNext();
               // } else if(event.isControlKeyDown() && event.getNativeKeyCode()
               // == KeyCodes.KEY_PAGEUP) { // bad in Firefox
            }
            else if (event.isAltKeyDown() && event.isUpArrow())
            {
               handlePrev();
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEDOWN)
            { // alt-pagedown
               handleNextState();
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_PAGEUP)
            { // alt-pageup
               handlePrevState();
            }
            else if (event.isAltKeyDown() && event.getNativeKeyCode() == 78)
            {
               if (toggleFuzzy.getValue())
                  toggleFuzzy.setValue(false);
               else
                  toggleFuzzy.setValue(true);
            }
            // these shortcuts disabled because they conflict with basic text editing:
//            else if (event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_HOME)
//            { // ctrl-home
//               cloneHandler.onClick(null);
//            }
//            else if (event.isControlKeyDown() && event.getNativeKeyCode() == KeyCodes.KEY_END)
//            { // ctrl-end
//               cloneAndSaveHandler.onClick(null);
//            }
         }

      });
      layoutTable.add(textArea);

      HorizontalPanel operationsPanel = new HorizontalPanel();
      operationsPanel.addStyleName("float-right-div");
      operationsPanel.setSpacing(4);
      layoutTable.add(operationsPanel);

      // icon as the current state of the unit
      // stateImage = new Image(resources.newUnit());
      // operationsPanel.add(stateImage);

      // Add content widget
      toggleFuzzy = new CheckBox(messages.fuzzy());
      operationsPanel.add(toggleFuzzy);

      PushButton cloneButton = new PushButton(new Image(), cloneHandler);
      cloneButton.setText(messages.editClone());
      cloneButton.setTitle(messages.editCloneShortcut());
      operationsPanel.add(cloneButton);

      PushButton cloneAndSaveButton = new PushButton(new Image(), cloneAndSaveHandler);
      cloneAndSaveButton.setText(messages.editCloneAndSave());
      cloneAndSaveButton.setTitle(messages.editCloneAndSaveShortcut());
      operationsPanel.add(cloneAndSaveButton);

      PushButton cancelButton = new PushButton(images.cellEditorCancel().createImage(), cancelHandler);
      cancelButton.setText(messages.editCancel());
      cancelButton.setTitle(messages.editCancelShortcut());
      operationsPanel.add(cancelButton);

      PushButton acceptButton = new PushButton(images.cellEditorAccept().createImage(), acceptHandler);
      acceptButton.setText(messages.editSave());
      acceptButton.setTitle(messages.editSaveShortcut());
      operationsPanel.add(acceptButton);
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

   private boolean isDirty()
   {
      if (cellValue == null)
         return false;
      return !textArea.getText().equals(cellValue.getTarget());
   }

   public boolean isEditing()
   {
      return cellValue != null;
   }

   public boolean isFocused()
   {
      return isFocused;
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
      if (isDirty())
      {
         callback.onCancel(cellEditInfo);
         return;
      }

      if (isEditing())
      {
         if (cellEditInfo.getCellIndex() == curCol && cellEditInfo.getRowIndex() == curRow)
         {
            return;
         }
         restoreView();
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

      int height = table.getWidget(curRow, curCol - 1).getOffsetHeight();

      int realHeight = height > MIN_HEIGHT ? height : MIN_HEIGHT;

      textArea.setHeight(realHeight + "px");

      int width = table.getWidget(curRow, curCol - 1).getOffsetWidth() - 10;
      textArea.setWidth(width + "px");

      table.setWidget(curRow, curCol, layoutTable);
      textArea.setText(cellValue.getTarget());

      this.cellValue = cellValue;
      textArea.setFocus(true);
      DOM.scrollIntoView(table.getCellFormatter().getElement(curRow, curCol));
      toggleFuzzy.setValue(cellValue.getStatus() == ContentState.NeedReview);
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
      if (cellValue.getTarget().isEmpty())
         cellValue.setStatus(ContentState.New);
      else
         cellValue.setStatus(toggleFuzzy.getValue() ? ContentState.NeedReview : ContentState.Approved);
      restoreView();

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

}
