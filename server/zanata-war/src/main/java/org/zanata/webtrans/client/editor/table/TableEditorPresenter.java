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

import static org.zanata.webtrans.client.editor.table.TableConstants.MAX_PAGE_ROW;
import static org.zanata.webtrans.client.editor.table.TableConstants.PAGE_SIZE;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.common.EditState;
import org.zanata.webtrans.client.action.UndoableTransUnitUpdateAction;
import org.zanata.webtrans.client.action.UndoableTransUnitUpdateHandler;
import org.zanata.webtrans.client.editor.DocumentEditorPresenter;
import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.client.events.CopySourceEvent;
import org.zanata.webtrans.client.events.CopySourceEventHandler;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.RedoFailureEvent;
import org.zanata.webtrans.client.events.TransMemoryCopyEvent;
import org.zanata.webtrans.client.events.TransMemoryCopyHandler;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.UndoAddEvent;
import org.zanata.webtrans.client.events.UndoFailureEvent;
import org.zanata.webtrans.client.events.UndoRedoFinishEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.EditingTranslationAction;
import org.zanata.webtrans.shared.rpc.EditingTranslationResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.gen2.table.client.TableModel;
import com.google.gwt.gen2.table.client.TableModel.Callback;
import com.google.gwt.gen2.table.client.TableModelHelper.Request;
import com.google.gwt.gen2.table.client.TableModelHelper.SerializableResponse;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TableEditorPresenter extends DocumentEditorPresenter<TableEditorPresenter.Display> implements HasPageNavigation
{
   public interface Display extends WidgetDisplay, HasPageNavigation
   {
      HasSelectionHandlers<TransUnit> getSelectionHandlers();

      HasPageChangeHandlers getPageChangeHandlers();

      HasPageCountChangeHandlers getPageCountChangeHandlers();

      RedirectingCachedTableModel<TransUnit> getTableModel();

      void setTableModelHandler(TableModelHandler<TransUnit> hadler);

      void reloadPage();

      void setPageSize(int size);

      void gotoRow(int row);

      void gotoRow(int row, boolean andEdit);

      TransUnit getTransUnitValue(int row);

      InlineTargetCellEditor getTargetCellEditor();

      List<TransUnit> getRowValues();

      boolean isFirstPage();

      boolean isLastPage();

      int getCurrentPage();

      int getPageSize();

      void setFindMessage(String findMessage);

      void startProcessing();

      void stopProcessing();

      /**
       * @return The index of the 'selected' row on the currently displayed
       *         page, or 0 if no row is selected
       */
      int getSelectedRowNumber();
   }

   private DocumentId documentId;

   private final CachingDispatchAsync dispatcher;
   private final Identity identity;
   private TransUnit selectedTransUnit;
   // private int lastRowNum;
   private List<Long> transIdNextNewFuzzyCache = new ArrayList<Long>();
   private List<Long> transIdPrevNewFuzzyCache = new ArrayList<Long>();

   private List<Long> transIdNextFuzzyCache = new ArrayList<Long>();
   private List<Long> transIdPrevFuzzyCache = new ArrayList<Long>();

   private List<Long> transIdNextNewCache = new ArrayList<Long>();
   private List<Long> transIdPrevNewCache = new ArrayList<Long>();

   private int curRowIndex;
   private int curPage;

   private String findMessage;

   private final TableEditorMessages messages;

   private UndoableTransUnitUpdateAction inProcessing;

   private final UndoableTransUnitUpdateHandler undoableTransUnitUpdateHandler = new UndoableTransUnitUpdateHandler()
   {
      @Override
      public void undo(final UndoableTransUnitUpdateAction action)
      {
         action.setUndo(true);
         action.setRedo(false);
         inProcessing = action;
         if (selectedTransUnit != null)
         {
            Log.info("cancel edit");
            display.getTargetCellEditor().cancelEdit();
         }
         dispatcher.rollback(action.getAction(), action.getResult(), new AsyncCallback<Void>()
         {
            @Override
            public void onFailure(Throwable e)
            {
               Log.error("UpdateTransUnit failure " + e, e);
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUpdateFailed(e.getLocalizedMessage())));
               inProcessing = null;
               // put back the old cell value
               display.getTableModel().clearCache();
               display.reloadPage();
               eventBus.fireEvent(new UndoFailureEvent(action));
            }

            @Override
            public void onSuccess(Void result)
            {
            }
         });
      }

      @Override
      public void redo(final UndoableTransUnitUpdateAction action)
      {
         action.setRedo(true);
         action.setUndo(false);
         inProcessing = action;
         final UpdateTransUnit updateTransUnit = action.getAction();
         updateTransUnit.setRedo(true);
         updateTransUnit.setVerNum(action.getResult().getCurrentVersionNum());
         dispatcher.execute(updateTransUnit, new AsyncCallback<UpdateTransUnitResult>()
         {
            @Override
            public void onFailure(Throwable e)
            {
               Log.error("redo failure " + e, e);
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUpdateFailed(e.getLocalizedMessage())));
               inProcessing = null;
               // put back the old cell value
               display.getTableModel().clearCache();
               display.reloadPage();
               eventBus.fireEvent(new RedoFailureEvent(action));
            }

            @Override
            public void onSuccess(UpdateTransUnitResult result)
            {
            }
         });
      }
   };

   @Inject
   public TableEditorPresenter(final Display display, final EventBus eventBus, final CachingDispatchAsync dispatcher, final Identity identity, final TableEditorMessages messages)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.identity = identity;
      this.messages = messages;
   }

   private void clearCacheList()
   {
      if (!transIdNextNewFuzzyCache.isEmpty())
         transIdNextNewFuzzyCache.clear();
      if (!transIdPrevNewFuzzyCache.isEmpty())
         transIdPrevNewFuzzyCache.clear();

      if (!transIdNextNewCache.isEmpty())
         transIdNextNewCache.clear();
      if (!transIdPrevNewCache.isEmpty())
         transIdPrevNewCache.clear();

      if (!transIdNextFuzzyCache.isEmpty())
         transIdNextFuzzyCache.clear();
      if (!transIdPrevFuzzyCache.isEmpty())
         transIdPrevFuzzyCache.clear();
   }

   private void reloadPage()
   {
      if (curPage != display.getCurrentPage())
      {
         display.getTargetCellEditor().cancelEdit();
         display.getTableModel().clearCache();
         display.reloadPage();
      }
   }
   @Override
   protected void onBind()
   {
      display.setTableModelHandler(tableModelHandler);
      display.setPageSize(TableConstants.PAGE_SIZE);
      registerHandler(display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>()
      {
         @Override
         public void onSelection(SelectionEvent<TransUnit> event)
         {
            TransUnit newSelectedItem = event.getSelectedItem();
            selectTransUnit(newSelectedItem);
         }
      }));

      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            if (!event.getDocument().getId().equals(documentId))
            {
               display.startProcessing();
               documentId = event.getDocument().getId();
               display.getTableModel().clearCache();
               display.getTableModel().setRowCount(TableModel.UNKNOWN_ROW_COUNT);
               display.gotoPage(0, true);
               display.stopProcessing();
            }
         }
      }));

      registerHandler(eventBus.addHandler(FindMessageEvent.getType(), new FindMessageHandler()
      {

         @Override
         public void onFindMessage(FindMessageEvent event)
         {
            Log.info("Find Message Event: " + event.getMessage());
            display.getTargetCellEditor().savePendingChange(true);
            if (selectedTransUnit != null)
            {
               Log.info("cancelling selection");
               display.getTargetCellEditor().clearSelection();
            }
            display.startProcessing();
            findMessage = event.getMessage();
            display.setFindMessage(findMessage);
            display.getTableModel().clearCache();
            display.getTableModel().setRowCount(TableModel.UNKNOWN_ROW_COUNT);
            display.gotoPage(0, true);
            display.stopProcessing();
         }

      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            if (documentId != null && documentId.equals(event.getDocumentId()))
            {
               // Clear the cache
               clearCacheList();

               // TODO this test never succeeds
               if (selectedTransUnit != null && selectedTransUnit.getId().equals(event.getTransUnit().getId()))
               {
                  // handle change in current selection
                  // eventBus.fireEvent(new NotificationEvent(Severity.Warning,
                  // "Someone else updated this translation unit. you're in trouble..."));
                  // display.getTableModel().setRowValue(row, rowValue);
                  Log.info("selected TU updated; cancelling edit");
                  display.getTargetCellEditor().cancelEdit();

                  // TODO reload page and return
               }

               final Integer rowOffset = getRowOffset(event.getTransUnit().getId());
               // - add TU index to model
               if (rowOffset != null)
               {
                  final int row = display.getCurrentPage() * display.getPageSize() + rowOffset;
                  Log.info("row calculated as " + row);
                  display.getTableModel().setRowValueOverride(row, event.getTransUnit());
                  if (inProcessing != null)
                  {
                     if (inProcessing.getAction().getTransUnitId().equals(event.getTransUnit().getId()))
                     {
                        Log.info("go to row:" + row);
                        tableModelHandler.gotoRow(row);
                        eventBus.fireEvent(new UndoRedoFinishEvent(inProcessing));
                        inProcessing = null;
                     }
                  }
                  else
                  {
                     reloadPage();
                  }
               }
               else
               {
                  display.getTableModel().clearCache();
                  display.getTargetCellEditor().cancelEdit();
                  if (inProcessing != null)
                  {
                     if (inProcessing.getAction().getTransUnitId().equals(event.getTransUnit().getId()))
                     {
                        int pageNum = inProcessing.getCurrentPage();
                        int rowNum = inProcessing.getRowNum();
                        int row = pageNum * PAGE_SIZE + rowNum;
                        Log.info("go to row:" + row);
                        Log.info("go to page:" + pageNum);
                        tableModelHandler.gotoRow(row);
                        eventBus.fireEvent(new UndoRedoFinishEvent(inProcessing));
                        inProcessing = null;
                     }
                  }
                  else
                  {
                     reloadPage();
                  }
               }
            }
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitEditEvent.getType(), new TransUnitEditEventHandler()
      {
         @Override
         public void onTransUnitEdit(TransUnitEditEvent event)
         {
            if (documentId != null && documentId.equals(event.getDocumentId()))
            {
               if (selectedTransUnit != null && selectedTransUnit.getId().equals(event.getTransUnitId()))
               {
                  // handle change in current selection
                  if (!event.getSessionId().equals(identity.getSessionId()))
                     eventBus.fireEvent(new NotificationEvent(Severity.Warning, messages.notifyInEdit()));
               }
               // display.getTableModel().clearCache();
               // display.reloadPage();
            }
         }
      }));

      registerHandler(eventBus.addHandler(NavTransUnitEvent.getType(), new NavTransUnitHandler()
      {
         @Override
         public void onNavTransUnit(NavTransUnitEvent event)
         {
            if (selectedTransUnit != null)
            {
               int step = event.getStep();
               Log.info("Step " + step);
               // Send message to server to stop editing current selection
               // stopEditing(selectedTransUnit);

               InlineTargetCellEditor editor = display.getTargetCellEditor();

               // If goto Next or Prev Fuzzy/New Trans Unit
               if (event.getRowType() == NavigationType.PrevEntry)
               {
                  editor.saveAndMoveRow(NavigationType.PrevEntry);
               }

               if (event.getRowType() == NavigationType.NextEntry)
               {
                  editor.saveAndMoveRow(NavigationType.NextEntry);
               }

               if (event.getRowType() == NavigationType.PrevState)
               {
                  editor.saveAndMoveNextState(NavigationType.PrevEntry);
               }

               if (event.getRowType() == NavigationType.NextState)
               {
                  editor.saveAndMoveNextState(NavigationType.NextEntry);
               }

               if (event.getRowType() == NavigationType.FirstEntry)
               {
                  editor.saveAndMoveRow(NavigationType.FirstEntry);
               }

               if (event.getRowType() == NavigationType.LastEntry)
               {
                  editor.saveAndMoveRow(NavigationType.LastEntry);
               }

            }
         }
      }));

      registerHandler(eventBus.addHandler(TransMemoryCopyEvent.getType(), new TransMemoryCopyHandler()
      {
         @Override
         public void onTransMemoryCopy(TransMemoryCopyEvent event)
         {
            // When user clicked on copy-to-target anchor, it checks
            // if user is editing any target. Notifies if not.
            if (display.getTargetCellEditor().isEditing())
            {
               display.getTargetCellEditor().setText(event.getTargetResult());
               display.getTargetCellEditor().autoSize();
               eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyCopied()));
            }
            else
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnopened()));
         }
      }));

      registerHandler(eventBus.addHandler(CopySourceEvent.getType(), new CopySourceEventHandler()
      {
         @Override
         public void onCopySource(CopySourceEvent event)
         {
            int rowOffset = getRowOffset(event.getTransUnit().getId());
            int row = display.getCurrentPage() * display.getPageSize() + rowOffset;

            tableModelHandler.gotoRow(row);
            display.getTargetCellEditor().setText(event.getTransUnit().getSource());
            display.getTargetCellEditor().autoSize();
         }

      }));

      Event.addNativePreviewHandler(new NativePreviewHandler()
      {
         @Override
         public void onPreviewNativeEvent(NativePreviewEvent event)
         {
            // Only when the Table is showed and editor is closed, the keyboard
            // event will be processed.
            if (display.asWidget().isVisible() && !display.getTargetCellEditor().isFocused())
            {
               NativeEvent nativeEvent = event.getNativeEvent();
               String nativeEventType = nativeEvent.getType();
               int keyCode = nativeEvent.getKeyCode();
               boolean shiftKey = nativeEvent.getShiftKey();
               boolean altKey = nativeEvent.getAltKey();
               boolean ctrlKey = nativeEvent.getCtrlKey();

               if (nativeEventType.equals("keydown") && altKey)
               {
                  if (keyCode == KeyCodes.KEY_UP || keyCode == TableConstants.KEY_J)
                  {
                     Log.info("Go to previous entry");
                     tableModelHandler.gotoPrevRow(false);
                     event.cancel();
                  }
                  else if (keyCode == KeyCodes.KEY_DOWN || keyCode == TableConstants.KEY_K)
                  {
                     Log.info("Go to next entry");
                     tableModelHandler.gotoNextRow(false);
                     event.cancel();
                  }
                  else if (keyCode == TableConstants.KEY_G)
                  {
                     if (selectedTransUnit != null)
                     {
                        Log.info("Copy from source");
                        tableModelHandler.gotoRow(curRowIndex);
                        display.getTargetCellEditor().cloneAction();
                     }
                  }
               }
               else if (nativeEventType.equals("keypress") && !shiftKey && !altKey && !ctrlKey)
               {
                  // PageDown key
                  switch (keyCode)
                  {
                  case KeyCodes.KEY_PAGEDOWN:
                     Log.info("Go to next page");
                     if (!display.isLastPage())
                        gotoNextPage();
                     event.cancel();
                     break;
                  // PageUp key
                  case KeyCodes.KEY_PAGEUP:
                     Log.info("Go to previous page");
                     if (!display.isFirstPage())
                        gotoPreviousPage();
                     event.cancel();
                     break;
                  // Home
                  case KeyCodes.KEY_HOME:
                     Log.info("Go to first page");
                     display.gotoFirstPage();
                     event.cancel();
                     break;
                  // End
                  case KeyCodes.KEY_END:
                     Log.info("Go to last page");
                     display.gotoLastPage();
                     event.cancel();
                     break;
                  default:
                     break;
                  }
               }
            }
         }
      });

      display.gotoFirstPage();
   }

   public Integer getRowOffset(TransUnitId transUnitId)
   {
      // TODO inefficient!
      for (int i = 0; i < display.getRowValues().size(); i++)
      {
         if (transUnitId.equals(display.getTransUnitValue(i).getId()))
         {
            Log.info("getRowOffset returning " + i);
            return i;
         }
      }
      return null;
   }

   private final TableModelHandler<TransUnit> tableModelHandler = new TableModelHandler<TransUnit>()
   {

      @Override
      public void requestRows(final Request request, final Callback<TransUnit> callback)
      {

         int numRows = request.getNumRows();
         int startRow = request.getStartRow();
         Log.info("Table requesting " + numRows + " starting from " + startRow);

         if (documentId == null)
         {
            callback.onFailure(new RuntimeException("No DocumentId"));
            return;
         }

         dispatcher.execute(new GetTransUnitList(documentId, startRow, numRows, findMessage), new AsyncCallback<GetTransUnitListResult>()
         {
            @Override
            public void onSuccess(GetTransUnitListResult result)
            {
               Log.info("find message:" + findMessage);
               SerializableResponse<TransUnit> response = new SerializableResponse<TransUnit>(result.getUnits());
               Log.info("Got " + result.getUnits().size() + " rows back");
               callback.onRowsReady(request, response);
               Log.info("Total of " + result.getTotalCount() + " rows available");
               display.getTableModel().setRowCount(result.getTotalCount());
            }

            @Override
            public void onFailure(Throwable caught)
            {
               if (caught instanceof AuthenticationError)
               {
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyNotLoggedIn()));
               }
               else if (caught instanceof AuthorizationError)
               {
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyLoadFailed()));
               }
               else
               {
                  Log.error("GetTransUnits failure " + caught, caught);
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUnknownError()));
               }
            }
         });
      }

      @Override
      public boolean onSetRowValue(int row, TransUnit rowValue)
      {
         final UpdateTransUnit updateTransUnit = new UpdateTransUnit(rowValue.getId(), rowValue.getTarget(), rowValue.getStatus());
         dispatcher.execute(updateTransUnit, new AsyncCallback<UpdateTransUnitResult>()
         {
            @Override
            public void onFailure(Throwable e)
            {
               Log.error("UpdateTransUnit failure " + e, e);
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUpdateFailed(e.getLocalizedMessage())));

               display.getTableModel().clearCache();
               display.reloadPage();
            }

            @Override
            public void onSuccess(UpdateTransUnitResult result)
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyUpdateSaved()));
               UndoableTransUnitUpdateAction undoAction = new UndoableTransUnitUpdateAction(updateTransUnit, result, curRowIndex, curPage);
               undoAction.setHandler(undoableTransUnitUpdateHandler);
               eventBus.fireEvent(new UndoAddEvent(undoAction));
            }
         });
         stopEditing(rowValue);
         return true;
      }

      public void onCancel(TransUnit rowValue)
      {
         // stopEditing(rowValue);
      }

      @Override
      public void updatePageAndRowIndex()
      {
         curPage = display.getCurrentPage();

         // Convert row number to row Index in table
         curRowIndex = curPage * TableConstants.PAGE_SIZE + display.getSelectedRowNumber();
         Log.info("Current Row Index" + curRowIndex);
      }

      @Override
      public void gotoNextRow(boolean andEdit)
      {
         updatePageAndRowIndex();
         int newRowIndex = curRowIndex + 1;
         if (newRowIndex < display.getTableModel().getRowCount())
         {
            if (andEdit)
            {
               gotoRow(newRowIndex);
            }
            else
            {
               gotoRow(newRowIndex, andEdit);
            }
         }
      }

      @Override
      public void gotoPrevRow(boolean andEdit)
      {
         updatePageAndRowIndex();
         int newRowIndex = curRowIndex - 1;
         if (newRowIndex >= 0)
         {
            if (andEdit)
            {
               gotoRow(newRowIndex);
            }
            else
            {
               gotoRow(newRowIndex, andEdit);
            }
         }
      }

      @Override
      public void gotoFirstRow()
      {
         updatePageAndRowIndex();
         gotoRow(0);
      }

      @Override
      public void gotoLastRow()
      {
         updatePageAndRowIndex();
         gotoRow(display.getTableModel().getRowCount() - 1);
      }

      @Override
      public void nextFuzzyNewIndex()
      {
         updatePageAndRowIndex();
         if (curRowIndex < display.getTableModel().getRowCount())
            gotoNextState(true, true);
      }

      @Override
      public void prevFuzzyNewIndex()
      {
         updatePageAndRowIndex();
         if (curRowIndex > 0)
            gotoPrevState(true, true);
      }

      @Override
      public void nextFuzzyIndex()
      {
         updatePageAndRowIndex();
         if (curRowIndex < display.getTableModel().getRowCount())
            gotoNextState(false, true);
      }

      @Override
      public void prevFuzzyIndex()
      {
         updatePageAndRowIndex();
         if (curRowIndex > 0)
            gotoPrevState(false, true);
      }

      @Override
      public void nextNewIndex()
      {
         updatePageAndRowIndex();
         if (curRowIndex < display.getTableModel().getRowCount())
            gotoNextState(true, false);
      }

      @Override
      public void prevNewIndex()
      {
         updatePageAndRowIndex();
         if (curRowIndex > 0)
            gotoPrevState(true, false);
      }

      @Override
      public void gotoRow(int rowIndex)
      {
         curPage = display.getCurrentPage();
         int pageNum = rowIndex / (MAX_PAGE_ROW + 1);
         int rowNum = rowIndex % (MAX_PAGE_ROW + 1);
         if (pageNum != curPage)
         {
            display.gotoPage(pageNum, false);
         }

         selectTransUnit(display.getTransUnitValue(rowNum));
         display.gotoRow(rowNum);

         if (pageNum != curPage)
         {
            display.getTargetCellEditor().cancelEdit();
         }
      }

      @Override
      public void gotoRow(int rowIndex, boolean andEdit)
      {
         curPage = display.getCurrentPage();
         int pageNum = rowIndex / (MAX_PAGE_ROW + 1);
         int rowNum = rowIndex % (MAX_PAGE_ROW + 1);
         if (pageNum != curPage)
         {
            display.gotoPage(pageNum, false);
         }

         selectTransUnit(display.getTransUnitValue(rowNum));
         display.gotoRow(rowNum, andEdit);
      }
   };

   private void stopEditing(TransUnit rowValue)
   {
      dispatcher.execute(new EditingTranslationAction(rowValue.getId(), EditState.StopEditing), new AsyncCallback<EditingTranslationResult>()
      {
         @Override
         public void onSuccess(EditingTranslationResult result)
         {
            // eventBus.fireEvent(new NotificationEvent(Severity.Warning,
            // "TransUnit Editing is finished"));
         }

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("EditingTranslationAction failure " + caught, caught);
            eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyStopFailed()));
         }

      });
   }

   boolean isReqComplete = true;

   private void cacheNextState(final NavigationCacheCallback callBack, final List<Long> cacheList, final boolean isNewState, final boolean isFuzzyState)
   {
      isReqComplete = false;
      dispatcher.execute(new GetTransUnitsNavigation(selectedTransUnit.getId().getId(), 3, false, findMessage, isNewState, isFuzzyState), new AsyncCallback<GetTransUnitsNavigationResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsNavigationResult result)
         {
            isReqComplete = true;
            if (!result.getUnits().isEmpty())
            {
               for (Long offset : result.getUnits())
               {
                  cacheList.add(offset + curRowIndex);
               }
               callBack.next(isNewState, isFuzzyState);
            }
         }

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("GetTransUnitsStates failure " + caught, caught);
         }
      });
   }

   private void cachePrevState(final NavigationCacheCallback callBack, final List<Long> cacheList, final boolean isNewState, final boolean isFuzzyState)
   {
      isReqComplete = false;
      dispatcher.execute(new GetTransUnitsNavigation(selectedTransUnit.getId().getId(), 3, true, findMessage, isNewState, isFuzzyState), new AsyncCallback<GetTransUnitsNavigationResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsNavigationResult result)
         {
            isReqComplete = true;
            if (!result.getUnits().isEmpty())
            {
               for (Long offset : result.getUnits())
               {
                  cacheList.add(curRowIndex - offset);
               }
               callBack.prev(isNewState, isFuzzyState);
            }
         }

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("GetTransUnitsStates failure " + caught, caught);
         }
      });
   }

   NavigationCacheCallback cacheCallback = new NavigationCacheCallback()
   {
      @Override
      public void next(boolean isNewState, boolean isFuzzyState)
      {
         gotoNextState(isNewState, isFuzzyState);
      }

      @Override
      public void prev(boolean isNewState, boolean isFuzzyState)
      {
         gotoPrevState(isNewState, isFuzzyState);
      }

   };

   private void gotoNextState(boolean isNewState, boolean isFuzzyState)
   {
      if (isNewState && isFuzzyState)
      {
         Log.info("go to Next Fuzzy Or Untranslated State");
         transIdPrevNewFuzzyCache.clear();
         gotoNextState(transIdNextNewFuzzyCache, true, true);
      }
      else if (isNewState)
      {
         Log.info("go to Next Untranslated State");
         transIdPrevNewCache.clear();
         gotoNextState(transIdNextNewCache, true, false);
      }
      else if (isFuzzyState)
      {
         Log.info("go to Next Fuzzy State");
         transIdPrevFuzzyCache.clear();
         gotoNextState(transIdNextFuzzyCache, false, true);
      }
   }

   private void gotoPrevState(boolean isNewState, boolean isFuzzyState)
   {
      if (isNewState && isFuzzyState)
      {
         Log.info("go to Prev Fuzzy Or Untranslated State");
         // Clean the cache for Next Fuzzy to avoid issues about cache is
         // obsolete
         transIdNextNewFuzzyCache.clear();
         gotoPrevState(transIdPrevNewFuzzyCache, true, true);
      }
      else if (isNewState)
      {
         Log.info("go to Prev Untranslated State");
         // Clean the cache for Next Fuzzy to avoid issues about cache is
         // obsolete
         transIdNextNewCache.clear();
         gotoPrevState(transIdPrevNewCache, true, false);
      }
      else if (isFuzzyState)
      {
         Log.info("go to Prev Fuzzy State");
         // Clean the cache for Next Fuzzy to avoid issues about cache is
         // obsolete
         transIdNextFuzzyCache.clear();
         gotoPrevState(transIdPrevFuzzyCache, false, true);
      }
   }

   private void gotoPrevState(List<Long> transIdPrevCache, boolean isNewState, boolean isFuzzyState)
   {
      // If the catch of row is empty and request is complete, generate
      // one
      if (transIdPrevCache.isEmpty())
      {
         if (isReqComplete)
            cachePrevState(cacheCallback, transIdPrevCache, isNewState, isFuzzyState);
      }
      else
      {
         int size = transIdPrevCache.size();
         int offset = transIdPrevCache.get(size - 1).intValue();
         if (curRowIndex > offset)
         {
            for (int i = 0; i < size; i++)
            {
               int newRowIndex = transIdPrevCache.get(i).intValue();
               if (curRowIndex > newRowIndex)
               {
                  display.getTargetCellEditor().cancelEdit();
                  tableModelHandler.gotoRow(newRowIndex);
                  break;
               }
            }
         }
         else
         {
            transIdPrevCache.clear();
            cachePrevState(cacheCallback, transIdPrevCache, isNewState, isFuzzyState);
         }
      }
   }

   private void gotoNextState(List<Long> transIdNextCache, boolean isNewState, boolean isFuzzyState)
   {
      // If the cache of next is empty, generate one
      if (transIdNextCache.isEmpty())
      {
         if (isReqComplete)
            cacheNextState(cacheCallback, transIdNextCache, isNewState, isFuzzyState);
      }
      else
      {
         int size = transIdNextCache.size();
         int offset = transIdNextCache.get(size - 1).intValue();
         if (curRowIndex < offset)
         {
            for (int i = 0; i < size; i++)
            {
               int newRowIndex = transIdNextCache.get(i).intValue();
               if (curRowIndex < newRowIndex)
               {
                  display.getTargetCellEditor().cancelEdit();
                  tableModelHandler.gotoRow(newRowIndex);
                  break;
               }
            }
         }
         else
         {
            transIdNextCache.clear();
            cacheNextState(cacheCallback, transIdNextCache, isNewState, isFuzzyState);
         }
      }
   }

   public TransUnit getSelectedTransUnit()
   {
      return selectedTransUnit;
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   @Override
   public void gotoFirstPage()
   {
      display.gotoFirstPage();
   }

   @Override
   public void gotoLastPage()
   {
      display.gotoLastPage();
   }

   @Override
   public void gotoNextPage()
   {
      display.gotoNextPage();
   }

   @Override
   public void gotoPage(int page, boolean forced)
   {
      display.gotoPage(page, forced);
   }

   @Override
   public void gotoPreviousPage()
   {
      display.gotoPreviousPage();
   }

   public void addPageChangeHandler(PageChangeHandler handler)
   {
      display.getPageChangeHandlers().addPageChangeHandler(handler);
   }

   public void addPageCountChangeHandler(PageCountChangeHandler handler)
   {
      display.getPageCountChangeHandlers().addPageCountChangeHandler(handler);
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   /**
    * Selects the given TransUnit and fires associated TU Selection event
    * 
    * @param transUnit the new TO to select
    */
   public void selectTransUnit(TransUnit transUnit)
   {
      tableModelHandler.updatePageAndRowIndex();
      if (selectedTransUnit == null || !transUnit.getId().equals(selectedTransUnit.getId()))
      {
         selectedTransUnit = transUnit;
         Log.info("SelectedTransUnit " + selectedTransUnit.getId());
         // Clean the cache when we click the new entry
         clearCacheList();

         eventBus.fireEvent(new TransUnitSelectionEvent(selectedTransUnit));
      }
   }
}
