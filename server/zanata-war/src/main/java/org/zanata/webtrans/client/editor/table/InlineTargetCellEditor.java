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

import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.CheckKey;
import org.zanata.webtrans.client.editor.CheckKeyImpl;
import org.zanata.webtrans.client.events.EditTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.ui.UserConfigConstants;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
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
         saveApprovedAndMoveNextState(NavigationType.NextEntry);
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
   private boolean isCancelButtonFocused = false;

   private boolean untranslatedMode = true, fuzzyMode = true;
   private boolean isEnterKeySavesEnabled = false, isEscKeyCloseEditor = false;

   private int curRow;
   private int curCol;
   private HTMLTable table;

   private String saveButtonShortcuts;
   private String saveButtonwithEnterShortcuts;
   private PushButton saveButton, fuzzyButton, cancelButton;

   /*
    * The minimum height of the target editor
    */
   // private static final int MIN_HEIGHT = 48;

   /**
    * Construct a new {@link InlineTargetCellEditor} with the specified images.
    */
   public InlineTargetCellEditor(final NavigationMessages messages, CancelCallback<TransUnit> callback, EditRowCallback rowCallback, final EventBus eventBus)
   {
      final CheckKey checkKey = new CheckKeyImpl(CheckKeyImpl.Context.Edit);
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

      // KeyDown is used to override browser event
      textArea.addKeyDownHandler(new KeyDownHandler()
      {
         @Override
         public void onKeyDown(KeyDownEvent event)
         {
            eventBus.fireEvent(new EditTransUnitEvent());
            checkKey.init(event.getNativeEvent());

            if (checkKey.isCopyFromSourceKey())
            {
               cloneAction();
            }
            else if (checkKey.isNextEntryKey())
            {
               // See editCell() for saving event
               saveAndMoveRow(NavigationType.NextEntry);
            }
            else if (checkKey.isPreviousEntryKey())
            {
               // See editCell() for saving event
               saveAndMoveRow(NavigationType.PrevEntry);
            }
            else if (checkKey.isNextStateEntryKey())
            {
               saveAndMoveNextState(NavigationType.NextEntry);
            }
            else if (checkKey.isPreviousStateEntryKey())
            {
               saveAndMoveNextState(NavigationType.PrevEntry);
            }
            else if (checkKey.isSaveAsFuzzyKey())
            {
               event.stopPropagation();
               event.preventDefault(); // stop browser save
               acceptFuzzyEdit();
            }
            else if (checkKey.isSaveAsApprovedKey(isEnterKeySavesEnabled))
            {
               event.stopPropagation();
               event.preventDefault();
               saveApprovedAndMoveNextState(NavigationType.NextEntry);
            }
            else if (checkKey.isCloseEditorKey(isEscKeyCloseEditor))
            {
               cancelEdit();
            }
            else if (checkKey.isUserTyping())
            {
               // Resize as user types
               autoSize();
            }
         }
      });
      layoutTable.add(textArea);

      operationsPanel = new HorizontalPanel();

      operationsPanel.addStyleName("float-right-div");
      operationsPanel.setSpacing(4);

      TableResources images = GWT.create(TableResources.class);

      saveButton = new PushButton(new Image(images.cellEditorAccept()));
      saveButton.setStyleName("gwt-Button");
      saveButtonShortcuts = messages.editSaveShortcut();
      saveButton.setTitle(messages.editSaveShortcut());
      saveButton.addClickHandler(acceptHandler);
      saveButtonwithEnterShortcuts = messages.editSavewithEnterShortcut();

      fuzzyButton = new PushButton(new Image(images.cellEditorFuzzy()));
      fuzzyButton.setStyleName("gwt-Button");
      fuzzyButton.setTitle(messages.saveAsFuzzy());
      fuzzyButton.addClickHandler(fuzzyHandler);

      cancelButton = new PushButton(new Image(images.cellEditorCancel()));
      cancelButton.setStyleName("gwt-Button");
      cancelButton.setTitle(messages.editCancelShortcut());
      cancelButton.addClickHandler(cancelHandler);
      cancelButton.addFocusHandler(new FocusHandler(){
         @Override
         public void onFocus(FocusEvent event)
         {
            isCancelButtonFocused = true;
         }
      });
      cancelButton.addBlurHandler(new BlurHandler(){

         @Override
         public void onBlur(BlurEvent event)
         {
            isCancelButtonFocused = false;
         }
      });

      operationsPanel.add(saveButton);
      operationsPanel.add(fuzzyButton);
      operationsPanel.add(cancelButton);
      layoutTable.add(operationsPanel);
   }

   public void cloneAction()
   {
      Log.info("InlineTargetCellEditor.java: Clone action.");
      textArea.setValue(cellValue.getSource(), true);
      textArea.setFocus(true);
   }

   public void gotoRow(NavigationType nav)
   {
      if (nav == NavigationType.NextEntry)
      {
         editRowCallback.gotoNextRow();
      }
      else if (nav == NavigationType.PrevEntry)
      {
         editRowCallback.gotoPrevRow();
      }
      else if (nav == NavigationType.FirstEntry)
      {
         editRowCallback.gotoFirstRow();
      }
      else if (nav == NavigationType.LastEntry)
      {
         editRowCallback.gotoLastRow();
      }
   }

   private void gotoNewRow(NavigationType nav)
   {
      if (nav == NavigationType.NextEntry)
      {
         editRowCallback.gotoNextNewRow();
      }
      else if (nav == NavigationType.PrevEntry)
      {
         editRowCallback.gotoPrevNewRow();
      }
   }

   private void gotoFuzzyAndNewRow(NavigationType nav)
   {
      if (nav == NavigationType.NextEntry)
      {
         editRowCallback.gotoNextFuzzyNewRow();
      }
      else if (nav == NavigationType.PrevEntry)
      {
         editRowCallback.gotoPrevFuzzyNewRow();
      }
   }

   private void gotoFuzzyRow(NavigationType nav)
   {
      if (nav == NavigationType.NextEntry)
      {
         editRowCallback.gotoNextFuzzyRow();
      }
      else if (nav == NavigationType.PrevEntry)
      {
         editRowCallback.gotoPrevFuzzyRow();
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

   public void saveAndMoveRow(NavigationType nav)
   {
      savePendingChange(true);
      gotoRow(nav);
   }

   public void saveAndMoveNextState(NavigationType nav)
   {
      savePendingChange(true);
      
      if (untranslatedMode && fuzzyMode)
      {
         gotoFuzzyAndNewRow(nav);
      }
      else if (!untranslatedMode && !fuzzyMode)
      {
         gotoRow(nav);
      }
      else if (untranslatedMode)
      {
         gotoNewRow(nav);
      }
      else if (fuzzyMode)
      {
         gotoFuzzyRow(nav);
      }
   }

   /**
    * save the contents of the cell as approved and move to next fuzzy or
    * untranslated
    */
   private void saveApprovedAndMoveNextState(NavigationType nav)
   {
      cellValue.setStatus(ContentState.Approved);
      acceptEdit();

      if (untranslatedMode && fuzzyMode)
      {
         gotoFuzzyAndNewRow(nav);
      }
      else if (untranslatedMode)
      {
         gotoNewRow(nav);
      }
      else if (fuzzyMode)
      {
         gotoFuzzyRow(nav);
      }
   }

   /**
    * Accept the contents of the cell editor as the new cell value.
    */
   private void acceptEdit()
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
      isFocused = false;

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
      isFocused = false;

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

   public void updateKeyBehaviour(Map<String, Boolean> configMap)
   {
      untranslatedMode = configMap.get(UserConfigConstants.BUTTON_UNTRANSLATED);
      fuzzyMode = configMap.get(UserConfigConstants.BUTTON_FUZZY);

      isEnterKeySavesEnabled = configMap.get(UserConfigConstants.BUTTON_ENTER);
      if (isEnterKeySavesEnabled)
      {
         saveButton.setTitle(saveButtonwithEnterShortcuts);
      }
      else
      {
         saveButton.setTitle(saveButtonShortcuts);
      }

      isEscKeyCloseEditor = configMap.get(UserConfigConstants.BUTTON_ESC);
   }

   public boolean isCancelButtonFocused()
   {
      return isCancelButtonFocused;
   }

   public void setCancelButtonFocused(boolean isCancelButtonFocused)
   {
      this.isCancelButtonFocused = isCancelButtonFocused;
      cancelButton.setFocus(isCancelButtonFocused);
   }
}
