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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.CheckKey;
import org.zanata.webtrans.client.editor.CheckKeyImpl;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.resources.EditorConfigConstants;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.gen2.table.client.CellEditor;
import com.google.gwt.gen2.table.override.client.HTMLTable;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

import javax.annotation.Nullable;

public class InlineTargetCellEditor implements CellEditor<TransUnit>
{
   /**
    * Default style name.
    */
   public static final String DEFAULT_STYLENAME = "gwt-TargetCellEditor";

   private static final int INITIAL_LINES = 3;
   private static final int HEIGHT_PER_LINE = 16;

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
//   private FlowPanel topLayoutPanel;

//   private VerticalPanel verticalPanel;
           
   private Widget cellViewWidget;

   private TransUnit cellValue;

//   private EditorTextArea textArea;
//   private HorizontalPanel operationsPanel;

   private boolean isFocused = false;
   private boolean isOpened = false;
   private boolean isCancelButtonFocused = false;
    private String findMessage;
    private boolean isReadOnly;
    private TargetContentsPresenter targetContentsPresenter;

    private boolean untranslatedMode = true, fuzzyMode = true;
   private boolean isEnterKeySavesEnabled = false, isEscKeyCloseEditor = false;

   private int curRow;
   private int curCol;
   private HTMLTable table;

   private String saveButtonShortcuts;
   private String saveButtonwithEnterShortcuts;
//   private PushButton saveButton, fuzzyButton, cancelButton, validateButton;
   // private ValidationMessagePanel validationMessagePanel;

   private boolean keypressed;
   private boolean typing;
   private int typingCycles;

   private final EventBus eventBus;
    

    /**
    * Construct a new {@link InlineTargetCellEditor} with the specified images.
    */
   public InlineTargetCellEditor(NavigationMessages messages, String findMessage, CancelCallback<TransUnit> callback, EditRowCallback rowCallback, final EventBus eventBus, final boolean isReadOnly, TargetContentsPresenter targetContentsPresenter)
   {
       this.findMessage = findMessage;
       this.isReadOnly = isReadOnly;
       this.targetContentsPresenter = targetContentsPresenter;
       final CheckKey checkKey = new CheckKeyImpl(CheckKeyImpl.Context.Edit);
      // Wrap contents in a table

      final int TYPING_TIMER_INTERVAL = 200; // ms
      final int TYPING_TIMER_RECURRENT_VALIDATION_PERIOD = 5; // intervals

//      verticalPanel = new VerticalPanel();
//      verticalPanel.setWidth("100%");
//      verticalPanel.setHeight("100%");
//
//      topLayoutPanel = new FlowPanel();
//      topLayoutPanel.setWidth("100%");
//

      this.eventBus = eventBus;
      cancelCallback = callback;
      editRowCallback = rowCallback;
//      textArea = new EditorTextArea();
//      textArea.setStyleName("TableEditorContent-Edit");
//
//      textArea.addValueChangeHandler(new ValueChangeHandler<String>()
//      {
//         @Override
//         public void onValueChange(ValueChangeEvent<String> event)
//         {
//            autoSize();
//            fireValidationEvent(eventBus);
//         }
//
//      });
//
//      final Timer typingTimer = new Timer()
//      {
//         @Override
//         public void run()
//         {
//            if (keypressed)
//            {
//               // still typing, validate periodically
//               keypressed = false;
//               typingCycles++;
//               if (typingCycles % TYPING_TIMER_RECURRENT_VALIDATION_PERIOD == 0)
//               {
//                  fireValidationEvent(eventBus);
//               }
//            }
//            else
//            {
//               // finished, validate immediately
//               this.cancel();
//               typing = false;
//               fireValidationEvent(eventBus);
//            }
//         }
//      };
//
//      // used to determine whether user is still typing
//      textArea.addKeyDownHandler(new KeyDownHandler()
//      {
//
//         @Override
//         public void onKeyDown(KeyDownEvent event)
//         {
//            if (typing)
//            {
//               keypressed = true;
//            }
//            else
//            {
//               // set false so that next keypress is detectable
//               keypressed = false;
//               typing = true;
//               typingCycles = 0;
//               typingTimer.scheduleRepeating(TYPING_TIMER_INTERVAL);
//            }
//         }
//      });
//
//      textArea.addBlurHandler(new BlurHandler()
//      {
//         @Override
//         public void onBlur(BlurEvent event)
//         {
//				isFocused = false;
//         }
//      });
//      textArea.addFocusHandler(new FocusHandler()
//      {
//         @Override
//         public void onFocus(FocusEvent event)
//         {
//            isFocused = true;
//         }
//      });
//
      // eventBus.addHandler(RequestValidationEvent.getType(), new
      // RequestValidationEventHandler()
      // {
      // @Override
      // public void onRequestValidation(RequestValidationEvent event)
      // {
      // if (isEditing())
      // {
      // fireValidationEvent(eventBus);
      // }
      // }
      // });

      // object creation is probably too much overhead for this, using simpler
      // boolean implementation.
      // textArea.addKeyUpHandler(new KeyUpHandler()
      // {
      // @Override
      // public void onKeyUp(KeyUpEvent event)
      // {
      // checkKey.init(event.getNativeEvent());
      // if (checkKey.isUserTyping())
      // {
      // runValidationTimer.schedule(REFRESH_INTERVAL);
      // }
      // }
      // });

      // KeyDown is used to override browser event
//      textArea.addKeyDownHandler(new KeyDownHandler()
//      {
//         @Override
//         public void onKeyDown(KeyDownEvent event)
//         {
//            eventBus.fireEvent(new EditTransUnitEvent());
//            checkKey.init(event.getNativeEvent());
//
//            if (checkKey.isCopyFromSourceKey())
//            {
//               cloneAction();
//            }
//            else if (checkKey.isNextEntryKey())
//            {
//               // See editCell() for saving event
//               saveAndMoveRow(NavigationType.NextEntry);
//            }
//            else if (checkKey.isPreviousEntryKey())
//            {
//               // See editCell() for saving event
//               saveAndMoveRow(NavigationType.PrevEntry);
//            }
//            else if (checkKey.isNextStateEntryKey())
//            {
//               saveAndMoveNextState(NavigationType.NextEntry);
//            }
//            else if (checkKey.isPreviousStateEntryKey())
//            {
//               saveAndMoveNextState(NavigationType.PrevEntry);
//            }
//            else if (checkKey.isSaveAsFuzzyKey())
//            {
//               event.stopPropagation();
//               event.preventDefault(); // stop browser save
//               acceptFuzzyEdit();
//            }
//            else if (checkKey.isSaveAsApprovedKey(isEnterKeySavesEnabled))
//            {
//               event.stopPropagation();
//               event.preventDefault();
//               saveApprovedAndMoveRow(NavigationType.NextEntry);
//            }
//            else if (checkKey.isCloseEditorKey(isEscKeyCloseEditor))
//            {
//               cancelEdit();
//            }
//            else if (checkKey.isUserTyping() && !checkKey.isBackspace())
//            {
//               growSize();
//            }
//            else if (checkKey.isUserTyping() && checkKey.isBackspace())
//            {
//               shrinkSize(false);
//            }
//         }
//      });
//
//      topLayoutPanel.add(textArea);
//
//      operationsPanel = new HorizontalPanel();
//
//      operationsPanel.addStyleName("float-right-div");
//      operationsPanel.setSpacing(4);
//
//      TableResources images = GWT.create(TableResources.class);
//
//      validateButton = new PushButton(new Image(images.cellEditorValidate()));
//      validateButton.setStyleName("gwt-Button");
//      validateButton.setTitle(messages.runValidation());
//      validateButton.addClickHandler(new ClickHandler()
//      {
//         @Override
//         public void onClick(ClickEvent event)
//         {
//            if (cellValue != null)
//            {
//               fireValidationEvent(eventBus);
//            }
//         }
//      });
//
//      saveButton = new PushButton(new Image(images.cellEditorAccept()));
//      saveButton.setStyleName("gwt-Button");
//      saveButtonShortcuts = messages.editSaveShortcut();
//      saveButton.setTitle(messages.editSaveShortcut());
//      saveButton.addClickHandler(acceptHandler);
//      saveButtonwithEnterShortcuts = messages.editSavewithEnterShortcut();
//
//      fuzzyButton = new PushButton(new Image(images.cellEditorFuzzy()));
//      fuzzyButton.setStyleName("gwt-Button");
//      fuzzyButton.setTitle(messages.saveAsFuzzy());
//      fuzzyButton.addClickHandler(fuzzyHandler);
//
//      cancelButton = new PushButton(new Image(images.cellEditorCancel()));
//      cancelButton.setStyleName("gwt-Button");
//      cancelButton.setTitle(messages.editCancelShortcut());
//      cancelButton.addClickHandler(cancelHandler);
//      cancelButton.addFocusHandler(new FocusHandler()
//      {
//         @Override
//         public void onFocus(FocusEvent event)
//         {
//            isCancelButtonFocused = true;
//         }
//      });
//      cancelButton.addBlurHandler(new BlurHandler()
//      {
//
//         @Override
//         public void onBlur(BlurEvent event)
//         {
//            isCancelButtonFocused = false;
//         }
//      });
//
//      operationsPanel.add(validateButton);
//      operationsPanel.add(saveButton);
//      operationsPanel.add(fuzzyButton);
//      operationsPanel.add(cancelButton);
//      topLayoutPanel.add(operationsPanel);
//
//      verticalPanel.add(topLayoutPanel);
//
      // validationMessagePanel = new ValidationMessagePanel(true, messages);
      // validationMessagePanel.setVisiblePolicy(true);
//
//      verticalPanel.add(validationMessagePanel);
//      verticalPanel.setCellVerticalAlignment(validationMessagePanel, HasVerticalAlignment.ALIGN_BOTTOM);
   }

   public void cloneAction()
   {
      Log.info("InlineTargetCellEditor.java: Clone action.");
//      textArea.setFocus(true);
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
//      if (curCellEditInfo != null && cellViewWidget != null)
//      {
//         curCellEditInfo.getTable().setWidget(curRow, curCol, cellViewWidget);
//         cellViewWidget.getParent().setHeight(cellViewWidget.getOffsetHeight() + "px");
//      }
       targetContentsPresenter.setToViewMode();
   }

   public boolean isEditing()
   {
      return cellValue != null && targetContentsPresenter.isEditing();
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
   {  //TODO copy TM or copy source will go here
      if (isEditing())
      {
         targetContentsPresenter.setCurrentEditorText(text);
      }
   }

   public void insertTextInCursorPosition(String text)
   {
      if (isEditing())
      {
          targetContentsPresenter.insertTextInCursorPosition(text);
//          String preCursor = currentEditor.getText().substring(0, textArea.getCursorPos());
//         String postCursor = textArea.getText().substring(textArea.getCursorPos(), textArea.getText().length());
         
//         textArea.setText(preCursor + text + postCursor);
      }
   }
   
   @Override
   public void editCell(CellEditInfo cellEditInfo, TransUnit cellValue, Callback<TransUnit> callback)
   {
      if (isReadOnly)
      {
         return;
      }

      if (isEditing())
      {
         if (cellEditInfo.getCellIndex() == curCol && cellEditInfo.getRowIndex() == curRow)
         {
            return;
         }
//         restoreView();
      }

      // save the content in previous cell before start new editing
      if (curRow != cellEditInfo.getRowIndex())
      {
         savePendingChange(false);
      }

      Log.debug("starting edit");

      // Save the current values
      curCallback = callback;
      curCellEditInfo = cellEditInfo;

      // Get the info about the cell
      table = curCellEditInfo.getTable();

      curRow = curCellEditInfo.getRowIndex();
      curCol = curCellEditInfo.getCellIndex();

//      cellViewWidget = table.getWidget(curRow, curCol);


//       table.setWidget(curRow, curCol, currentTargetPresenter.getDisplay().asWidget());

       this.cellValue = cellValue;
       targetContentsPresenter.showEditors(curRow);

//      textArea.setFocus(true);
      isOpened = true;

      DOM.scrollIntoView(table.getCellFormatter().getElement(curRow, curCol));

      // hide until validation results are available
      // validationMessagePanel.setVisible(false);
      fireValidationEvent(eventBus);
   }

   public void savePendingChange(boolean cancelIfUnchanged)
   {
      // if something has changed, save as approved
//      if (isEditing() && !cellValue.getTarget().equals(textArea.getText()))
      if (isEditing() && targetContentsPresenter.getCurrentDisplay() != null)
      {
         List<String> newTargets = targetContentsPresenter.getNewTargets();
         if (!cellValue.getTargets().equals(newTargets))
         {
            Log.debug("savePendingChange - acceptEdit");
            acceptEdit();
         }
      }
      else if (cancelIfUnchanged)
      {
         Log.debug("savePendingChange- cancel edit");
         cancelEdit();
      }
      targetContentsPresenter.setToViewMode();
   }

   private void determineStatus(List<String> newTargets, ContentState stateToSet)
   {
      Collection<String> emptyTargets = Collections2.filter(newTargets, new Predicate<String>()
      {
         @Override
         public boolean apply(@Nullable String input)
         {
            return Strings.isNullOrEmpty(input);
         }
      });
      if (emptyTargets.isEmpty() && stateToSet == ContentState.Approved)
      {
         cellValue.setStatus(ContentState.Approved);
      } 
      else if (emptyTargets.size() > 0 && stateToSet == ContentState.NeedReview)
      {
         cellValue.setStatus(ContentState.NeedReview);
      }
      else
      {
         cellValue.setStatus(ContentState.New);
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
   private void saveApprovedAndMoveRow(NavigationType nav)
   {
      cellValue.setStatus(ContentState.Approved);
      acceptEdit();
      gotoRow(nav);
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
      List<String> newTargets = targetContentsPresenter.getNewTargets();
      cellValue.setTargets(newTargets);
      determineStatus(newTargets, ContentState.Approved);

      restoreView();
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
//      String text = textArea.getText();
      List<String> newTargets = targetContentsPresenter.getNewTargets();
      cellValue.setTargets(newTargets);
      determineStatus(newTargets, ContentState.NeedReview);
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
//      textArea.setFocus(false);
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
//      shrinkSize(true);
//      growSize();
   }

//   /**
//    * forceShrink will resize the textArea to initialLines(3 lines) and growSize
//    * according to the scroll height
//    *
//    * @param forceShrink
//    */
//   private void shrinkSize(boolean forceShrink)
//   {
//      if (forceShrink)
//      {
//         textArea.setVisibleLines(INITIAL_LINES);
//      }
//      else
//      {
//         if (textArea.getElement().getScrollHeight() <= (INITIAL_LINES * HEIGHT_PER_LINE))
//         {
//            textArea.setVisibleLines(INITIAL_LINES);
//         }
//         else
//         {
//            if (textArea.getElement().getScrollHeight() >= textArea.getElement().getClientHeight())
//            {
//               int newHeight = textArea.getElement().getScrollHeight() - textArea.getElement().getClientHeight() > 0 ? textArea.getElement().getScrollHeight() - textArea.getElement().getClientHeight() : HEIGHT_PER_LINE;
//               int newLine = (newHeight / HEIGHT_PER_LINE) - 1 > INITIAL_LINES ? (newHeight / HEIGHT_PER_LINE) - 1 : INITIAL_LINES;
//               textArea.setVisibleLines(textArea.getVisibleLines() - newLine);
//            }
//            growSize();
//         }
//      }
//   }
//
//   private void growSize()
//   {
//      if (textArea.getElement().getScrollHeight() > textArea.getElement().getClientHeight())
//      {
//         int newHeight = textArea.getElement().getScrollHeight() - textArea.getElement().getClientHeight();
//         int newLine = (newHeight / HEIGHT_PER_LINE) + 1;
//         textArea.setVisibleLines(textArea.getVisibleLines() + newLine);
//      }
//   }

   public void setShowOperationButtons(boolean showButtons)
   {
//      operationsPanel.setVisible(showButtons);
   }

   public void updateKeyBehaviour(Map<String, Boolean> configMap)
   {
      if (configMap.containsKey(EditorConfigConstants.BUTTON_FUZZY) && configMap.containsKey(EditorConfigConstants.BUTTON_UNTRANSLATED))
      {
         untranslatedMode = configMap.get(EditorConfigConstants.BUTTON_UNTRANSLATED);
         fuzzyMode = configMap.get(EditorConfigConstants.BUTTON_FUZZY);
      }

//      if (configMap.containsKey(EditorConfigConstants.BUTTON_ENTER))
//      {
//         isEnterKeySavesEnabled = configMap.get(EditorConfigConstants.BUTTON_ENTER);
//         if (isEnterKeySavesEnabled)
//         {
//            saveButton.setTitle(saveButtonwithEnterShortcuts);
//         }
//         else
//         {
//            saveButton.setTitle(saveButtonShortcuts);
//         }
//      }

      if (configMap.containsKey(EditorConfigConstants.BUTTON_ESC))
      {
         isEscKeyCloseEditor = configMap.get(EditorConfigConstants.BUTTON_ESC);
      }

   }

   public boolean isCancelButtonFocused()
   {
      return isCancelButtonFocused;
   }

   public void setCancelButtonFocused(boolean isCancelButtonFocused)
   {
      this.isCancelButtonFocused = isCancelButtonFocused;
//      cancelButton.setFocus(isCancelButtonFocused);
   }

   public TransUnit getTargetCell()
   {
      return cellValue;
   }

   public void updateValidationMessagePanel(List<String> errors)
   {
      // validationMesCsagePanel.setContent(errors);
      // validationMessagePanel.setVisible(true);
   }

   /**
    * @param eventBus
    */
   private void fireValidationEvent(final EventBus eventBus)
   {
       // TODO Plural Support
//      eventBus.fireEvent(new RunValidationEvent(cellValue.getId(), cellValue.getSources(), currentTargetPresenter.getDisplay().getCurrentEditor().getText(), false));
   }
   
   public void setReadOnly(boolean isReadOnly)
   {
      this.isReadOnly = isReadOnly;
      // cancelEdit();
   }
}
