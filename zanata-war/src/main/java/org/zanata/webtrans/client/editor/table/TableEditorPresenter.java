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

import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.OpenEditorEvent;
import org.zanata.webtrans.client.events.OpenEditorEventHandler;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.TransUnitNavigationService;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.FilterViewConfirmationPanel;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.UpdateType;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Predicate;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class TableEditorPresenter extends WidgetPresenter<TableEditorPresenter.Display> implements HasPageNavigation
{
   public interface Display extends WidgetDisplay, HasPageNavigation
   {
      HasSelectionHandlers<TransUnit> getSelectionHandlers();

      HasPageChangeHandlers getPageChangeHandlers();

      HasPageCountChangeHandlers getPageCountChangeHandlers();

      RedirectingCachedTableModel<TransUnit> getTableModel();

      void setTableModelHandler(TableModelHandler<TransUnit> handler);

      void reloadPage();

      void setPageSize(int size);

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

      void setTransUnitDetails(TransUnit selectedTransUnit);

      boolean isProcessing();

      void ignoreStopProcessing();

      TransUnit getRowValue(int row);

      void updateRowBorder(int row, String color);

      void resetRowBorder(int row);
   }

   private DocumentId documentId;

   private final CachingDispatchAsync dispatcher;
   private final Identity identity;
   private TransUnit selectedTransUnit;
   private TransUnitId targetTransUnitId;

   private String findMessage;

   private final TableEditorMessages messages;

   private final FilterViewConfirmationPanel filterViewConfirmationPanel = new FilterViewConfirmationPanel();

   private final WorkspaceContext workspaceContext;

   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;

   private final UserSessionService sessionService;
   private final Provider<UndoLink> undoLinkProvider;

   private UserConfigHolder configHolder;
   private Scheduler scheduler;

   private boolean filterTranslated, filterNeedReview, filterUntranslated;

   private final TransUnitNavigationService navigationService;

   @Inject
   public TableEditorPresenter(final Display display, final EventBus eventBus, final CachingDispatchAsync dispatcher, final Identity identity, final TableEditorMessages messages, final WorkspaceContext workspaceContext, final SourceContentsPresenter sourceContentsPresenter, final TargetContentsPresenter targetContentsPresenter, UserConfigHolder configHolder, Scheduler scheduler, TransUnitNavigationService navigationService, final UserSessionService sessionService, Provider<UndoLink> undoLinkProvider)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.identity = identity;
      this.messages = messages;
      this.workspaceContext = workspaceContext;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.configHolder = configHolder;
      this.scheduler = scheduler;
      this.navigationService = navigationService;
      this.sessionService = sessionService;

      this.undoLinkProvider = undoLinkProvider;
   }

   /**
    * Clear all current transUnit list and re-query from server. Force to run
    * requestRows@TableModelHandler
    */
   public void initialiseTransUnitList()
   {
      display.getTableModel().clearCache();
      display.getTableModel().setRowCount(TableModel.UNKNOWN_ROW_COUNT);
      display.gotoPage(0, true);

      // modal navigation disabled if there's findMessage
      if (findMessage == null || findMessage.isEmpty())
      {
         initialiseTransUnitsNavigation();
         eventBus.fireEvent(new EnableModalNavigationEvent(true));
      }
      else
      {
         eventBus.fireEvent(new EnableModalNavigationEvent(false));
      }

   }

   private void initialiseTransUnitsNavigation()
   {
      dispatcher.execute(new GetTransUnitsNavigation(documentId.getValue(), findMessage, filterViewConfirmationPanel.isFilterUntranslated(), filterViewConfirmationPanel.isFilterNeedReview(), filterViewConfirmationPanel.isFilterTranslated()), new AsyncCallback<GetTransUnitsNavigationResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsNavigationResult result)
         {
            navigationService.init(result.getTransIdStateList(), result.getIdIndexList(), display.getPageSize());
         }

         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("GetTransUnitsNavigation failure " + caught, caught);
         }
      });
   }

   @Override
   protected void onBind()
   {
      display.setTableModelHandler(tableModelHandler);
      display.setPageSize(TableConstants.PAGE_SIZE);

      registerHandler(filterViewConfirmationPanel.getSaveChangesAndFilterButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            saveChangesAndFilter();
         }
      }));

      registerHandler(filterViewConfirmationPanel.getSaveFuzzyAndFilterButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            saveFuzzyAndFilter();
         }
      }));

      registerHandler(filterViewConfirmationPanel.getDiscardChangesAndFilterButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            discardChangesAndFilter();
         }
      }));

      registerHandler(filterViewConfirmationPanel.getCancelFilterButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            cancelFilter();
         }
      }));

      registerHandler(eventBus.addHandler(FilterViewEvent.getType(), new FilterViewEventHandler()
      {
         @Override
         public void onFilterView(FilterViewEvent event)
         {
            filterTransUnitsView(event);
         }
      }));

      registerHandler(display.getSelectionHandlers().addSelectionHandler(new SelectionHandler<TransUnit>()
      {
         @Override
         public void onSelection(SelectionEvent<TransUnit> event)
         {
            if (event.getSelectedItem() != null)
            {
               display.getTargetCellEditor().savePendingChange(true);
               selectTransUnit(event.getSelectedItem(), true);
            }
         }
      }));

      registerHandler(eventBus.addHandler(DocumentSelectionEvent.getType(), new DocumentSelectionHandler()
      {
         @Override
         public void onDocumentSelected(DocumentSelectionEvent event)
         {
            loadDocument(event.getDocumentId());
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
            findMessage = event.getMessage();
            display.setFindMessage(findMessage);
            if (selectedTransUnit != null)
            {
               targetTransUnitId = selectedTransUnit.getId();
            }
            initialiseTransUnitList();
         }

      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
            Log.debug("onTransUnitUpdated(TransUnitUpdatedEvent)");

            Log.debug("event.getUpdateType: " + event.getUpdateType());
            // assume update was successful
            if (documentId != null && documentId.equals(event.getUpdateInfo().getDocumentId()))
            {
               navigationService.updateMap(event.getUpdateInfo().getTransUnit().getId().getId(), event.getUpdateInfo().getTransUnit().getStatus());

               boolean editing = targetContentsPresenter.isEditing();
               Integer rowIndex = navigationService.getRowIndex(event.getUpdateInfo().getTransUnit(), isFiltering(), display.getRowValues());
               boolean updateRow = true;
               boolean reopen = false;

               boolean updatingSelectedTU = selectedTransUnit != null && selectedTransUnit.getId().equals(event.getUpdateInfo().getTransUnit().getId());
               if (updatingSelectedTU)
               {
                  Log.info("selected TU updated");
                  boolean sameEditorClientId = event.getEditorClientId().equals(identity.getEditorClientId());
                  if (sameEditorClientId)
                  {
                     // if save-as-fuzzy on same tab
                     if (event.getUpdateType() == UpdateType.WebEditorSaveFuzzy)
                     {
                        updateRow = false;
                        reopen = true;

                        // stay focused if same client id fuzzy edit
                        if (rowIndex != null)
                        {
                           TransUnit rowValue = display.getRowValue(rowIndex);
                           if (rowValue != null)
                           {
                              rowValue.OverrideWith(event.getUpdateInfo().getTransUnit());
                              display.getTableModel().setRowValueOverride(rowIndex, event.getUpdateInfo().getTransUnit());
                           }
                        }
                     }
                     else
                     {
                        // current client updated current TU (probably Replace, possibly save as Approved)
                        // will kick out of editor (and clobber local changes)
                     }
                  }
                  else
                  {
                     // another client updated current TU
                     // will kick out of editor (and clobber local changes)
                  }
               }
               else
               {
                  // updateRow = true, value updated below
                  // re-open editor to keep editor focused despite model update closing editor
                  reopen = true;
               }
               Log.debug("reopen: " + reopen);
               Log.debug("updateRow: " + updateRow);
               if (updateRow && rowIndex != null)
               {
                  Log.info("onTransUnitUpdated - update row:" + rowIndex);
                  display.getTableModel().setRowValueOverride(rowIndex, event.getUpdateInfo().getTransUnit());
               }
               if (editing && reopen)
               {
                  Log.debug("going to current row");
                  gotoCurrentRow();
               }
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
               // int step = event.getStep();
               // Send message to server to stop editing current
               // selection
               // stopEditing(selectedTransUnit);

               // If goto Next or Prev Fuzzy/New Trans Unit
               if (event.getRowType() == NavigationType.PrevEntry)
               {

                  targetContentsPresenter.movePrevious(false);
               }

               if (event.getRowType() == NavigationType.NextEntry)
               {
                  targetContentsPresenter.moveNext(false);
               }

               if (event.getRowType() == NavigationType.PrevState)
               {
                  targetContentsPresenter.moveToNextState(NavigationType.PrevEntry);
               }

               if (event.getRowType() == NavigationType.NextState)
               {
                  targetContentsPresenter.moveToNextState(NavigationType.NextEntry);
               }

               if (event.getRowType() == NavigationType.FirstEntry)
               {
                  targetContentsPresenter.saveAndMoveRow(NavigationType.FirstEntry);
               }

               if (event.getRowType() == NavigationType.LastEntry)
               {
                  targetContentsPresenter.saveAndMoveRow(NavigationType.LastEntry);
               }

            }
         }
      }));

      registerHandler(eventBus.addHandler(OpenEditorEvent.getType(), new OpenEditorEventHandler()
      {
         @Override
         public void onOpenEditor(OpenEditorEvent event)
         {
            tableModelHandler.gotoRowInCurrentPage(event.getRowNum(), true);
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitEditEvent.getType(), new TransUnitEditEventHandler()
      {
         @Override
         public void onTransUnitEdit(TransUnitEditEvent event)
         {
            if (identity.getEditorClientId().getValue().equals(event.getEditorClientId().getValue()))
            {
               Integer prevRow = navigationService.getRowNumber(event.getPrevSelectedTransUnit(), display.getRowValues());
               if (prevRow != null)
               {
                  resetBorder(prevRow, event.getEditorClientId());
               }

               Integer row = navigationService.getRowNumber(event.getSelectedTransUnit(), display.getRowValues());
               if (row != null)
               {
                  display.updateRowBorder(row, sessionService.getColor(event.getEditorClientId().getValue()));
               }
            }
            else
            {
               // check if the row is editing/selected by you, if yes,
               // ignore, else as above
               Integer prevRow = navigationService.getRowNumber(event.getPrevSelectedTransUnit(), display.getRowValues());
               if (prevRow != null && navigationService.getCurrentRowNumber() != prevRow)
               {
                  resetBorder(prevRow, event.getEditorClientId());
               }

               Integer row = navigationService.getRowNumber(event.getSelectedTransUnit(), display.getRowValues());
               if (row != null && navigationService.getCurrentRowNumber() != row)
               {
                  display.updateRowBorder(row, sessionService.getColor(event.getEditorClientId().getValue()));
               }
            }

         }
      }));

      registerHandler(eventBus.addHandler(ExitWorkspaceEvent.getType(), new ExitWorkspaceEventHandler()
      {
         @Override
         public void onExitWorkspace(ExitWorkspaceEvent event)
         {
            TransUnit tu = sessionService.getUserPanel(event.getEditorClientId()).getSelectedTransUnit();
            Integer row = navigationService.getRowNumber(tu, display.getRowValues());
            if (row != null)
            {
               resetBorder(row, event.getEditorClientId());
            }
         }
      }));

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            boolean readOnly = event.isReadOnly();
            workspaceContext.setReadOnly(readOnly);
            configHolder.setDisplayButtons(false);
            eventBus.fireEvent(new UserConfigChangeEvent());
            display.getTargetCellEditor().setReadOnly(readOnly);

            if (readOnly)
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyReadOnlyWorkspace()));
            }
            else
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyEditableWorkspace()));
            }
         }
      }));

      display.gotoFirstPage();

      History.fireCurrentHistoryState();
   }

   private void resetBorder(int prevRow, EditorClientId editorClientId)
   {
      // Check if other users is in that row
      for (Map.Entry<EditorClientId, UserPanelSessionItem> entry : sessionService.getUserSessionMap().entrySet())
      {
         if (entry.getValue().getSelectedTransUnit() != null && !editorClientId.getValue().equals(entry.getKey()))
         {
            Integer row = navigationService.getRowNumber(entry.getValue().getSelectedTransUnit(), display.getRowValues());
            if (row != null && row == prevRow)
            {
               display.updateRowBorder(prevRow, sessionService.getColor(entry.getKey().getValue()));
               return;
            }
         }
      }

      display.resetRowBorder(prevRow);

   }

   private void filterTransUnitsView(FilterViewEvent event)
   {
      if (!event.isCancelFilter())
      {
         filterTranslated = event.isFilterTranslated();
         filterNeedReview = event.isFilterNeedReview();
         filterUntranslated = event.isFilterUntranslated();

         if (shouldPopUpConfirmation())
         {
            filterViewConfirmationPanel.center();
         }
         else
         {
            hideConfirmationPanelAndDoFiltering();
         }
      }
   }

   private boolean shouldPopUpConfirmation()
   {
      InlineTargetCellEditor targetCellEditor = display.getTargetCellEditor();
      return targetCellEditor.isOpened() && targetCellEditor.isEditing() && targetCellEditor.hasTargetContentsChanged();
   }

   private void saveChangesAndFilter()
   {
      Log.info("Save changes and filter");
      display.getTargetCellEditor().savePendingChange(true);
      hideConfirmationPanelAndDoFiltering();
   }

   private void saveFuzzyAndFilter()
   {
      Log.info("Save changes as fuzzy and filter");
      display.getTargetCellEditor().acceptFuzzyEdit();
      hideConfirmationPanelAndDoFiltering();
   }

   private void discardChangesAndFilter()
   {
      Log.info("Discard changes and filter");
      display.getTargetCellEditor().cancelEdit();
      hideConfirmationPanelAndDoFiltering();
   }

   private void hideConfirmationPanelAndDoFiltering()
   {
      filterViewConfirmationPanel.updateFilter(filterTranslated, filterNeedReview, filterUntranslated);
      filterViewConfirmationPanel.hide();

      if (selectedTransUnit != null)
      {
         targetTransUnitId = selectedTransUnit.getId();
      }
      initialiseTransUnitList();
   }

   private void cancelFilter()
   {
      Log.info("Cancel filter");
      eventBus.fireEvent(new FilterViewEvent(filterViewConfirmationPanel.isFilterTranslated(), filterViewConfirmationPanel.isFilterNeedReview(), filterViewConfirmationPanel.isFilterUntranslated(), true));
      filterViewConfirmationPanel.hide();
   }

   public boolean isFiltering()
   {
      return (findMessage != null && !findMessage.isEmpty()) || (filterViewConfirmationPanel.isFilterTranslated() || filterViewConfirmationPanel.isFilterNeedReview() || filterViewConfirmationPanel.isFilterUntranslated());
   }

   private final TableModelHandler<TransUnit> tableModelHandler = new TableModelHandler<TransUnit>()
   {

      @Override
      public void requestRows(final Request request, final Callback<TransUnit> callback)
      {
         int numRows = request.getNumRows();
         int startRow = request.getStartRow();

         if (documentId == null)
         {
            callback.onFailure(new RuntimeException("No DocumentId"));
            return;
         }
         Log.info("Table requesting " + numRows + " starting from " + startRow);

         if (display.isProcessing())
         {
            display.ignoreStopProcessing();
         }
         else
         {
            display.startProcessing();
         }

         dispatcher.execute(new GetTransUnitList(documentId, startRow, numRows, findMessage, filterViewConfirmationPanel.isFilterTranslated(), filterViewConfirmationPanel.isFilterNeedReview(), filterViewConfirmationPanel.isFilterUntranslated(), targetTransUnitId), new AsyncCallback<GetTransUnitListResult>()
         {
            @Override
            public void onSuccess(GetTransUnitListResult result)
            {
               targetContentsPresenter.initWidgets(display.getPageSize());
               sourceContentsPresenter.initWidgets(display.getPageSize());
               SerializableResponse<TransUnit> response = new SerializableResponse<TransUnit>(result.getUnits());
               Log.info("Got " + result.getUnits().size() + " rows back of " + result.getTotalCount() + " available");
               callback.onRowsReady(request, response);
               display.getTableModel().setRowCount(result.getTotalCount());

               int gotoRow = navigationService.getCurrentRowIndex();

               if (result.getUnits().size() > 0)
               {
                  if (result.getGotoRow() != -1)
                  {
                     gotoRow = result.getGotoRow();
                  }
                  tableModelHandler.gotoRow(gotoRow, false);
               }
               display.stopProcessing();
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
                  eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyLoadFailed()));
               }
               display.stopProcessing();
            }
         });
         targetTransUnitId = null;
      }

      @Override
      public boolean onSetRowValue(final int row, TransUnit rowValue)
      {
         UpdateType updateType = rowValue.getStatus() == ContentState.Approved ? UpdateType.WebEditorSave : UpdateType.WebEditorSaveFuzzy;
         Log.debug("row updated, calculated update type: " + updateType);
         final UpdateTransUnit updateTransUnit = new UpdateTransUnit(new TransUnitUpdateRequest(rowValue.getId(), rowValue.getTargets(), rowValue.getStatus(), rowValue.getVerNum()), updateType);
         dispatcher.execute(updateTransUnit, new AsyncCallback<UpdateTransUnitResult>()
         {
            @Override
            public void onFailure(Throwable e)
            {
               Log.error("UpdateTransUnit failure " + e, e);
               String message = e.getLocalizedMessage();
               failure(message);
            }

            /**
             * @param message
             */
            private void failure(String message)
            {
               eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.notifyUpdateFailed(message)));
               display.getTableModel().clearCache();
               display.reloadPage();
            }

            @Override
            public void onSuccess(UpdateTransUnitResult result)
            {
               // FIXME check result.success
               if (result.isSingleSuccess())
               {
                  UndoLink undoLink = undoLinkProvider.get();
                  undoLink.prepareUndoFor(result);
                  targetContentsPresenter.addUndoLink(row, undoLink);
                  eventBus.fireEvent(new NotificationEvent(Severity.Info, messages.notifyUpdateSaved()));
               }
               else
               {
                  // TODO localised message
                  failure("row " + row);
               }
            }
         });
         return true;
      }

      public void onCancel(TransUnit rowValue)
      {
      }

      @Override
      public void gotoNextRow(boolean andEdit)
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         int newRowIndex = navigationService.getNextRowIndex();
         if (newRowIndex < display.getTableModel().getRowCount())
         {
            gotoRow(newRowIndex, andEdit);
         }
      }

      @Override
      public void gotoPrevRow(boolean andEdit)
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         int newRowIndex = navigationService.getPrevRowIndex();
         if (newRowIndex >= 0)
         {
            gotoRow(newRowIndex, andEdit);
         }
      }

      @Override
      public void gotoFirstRow()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         gotoRow(0, true);
      }

      @Override
      public void gotoLastRow()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         gotoRow(display.getTableModel().getRowCount() - 1, true);
      }

      @Override
      public void gotoCurrentRow(boolean andEdit)
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         gotoRow(navigationService.getCurrentRowIndex(), andEdit);
      }

      @Override
      public void nextFuzzyNewIndex()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         if (navigationService.getCurrentRowIndex() < display.getTableModel().getRowCount())
         {
            gotoNextState(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE);
         }
      }

      @Override
      public void prevFuzzyNewIndex()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         if (navigationService.getCurrentRowIndex() > 0)
         {
            gotoPrevState(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE);
         }
      }

      @Override
      public void nextFuzzyIndex()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         if (navigationService.getCurrentRowIndex() < display.getTableModel().getRowCount())
         {
            gotoNextState(TransUnitNavigationService.FUZZY_PREDICATE);
         }
      }

      @Override
      public void prevFuzzyIndex()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         if (navigationService.getCurrentRowIndex() > 0)
         {
            gotoPrevState(TransUnitNavigationService.FUZZY_PREDICATE);
         }
      }

      @Override
      public void nextNewIndex()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         if (navigationService.getCurrentRowIndex() < display.getTableModel().getRowCount())
         {
            gotoNextState(TransUnitNavigationService.NEW_PREDICATE);
         }
      }

      @Override
      public void prevNewIndex()
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         if (navigationService.getCurrentRowIndex() > 0)
         {
            gotoPrevState(TransUnitNavigationService.NEW_PREDICATE);
         }
      }

      @Override
      public void gotoRow(int rowIndex, boolean andEdit)
      {
         navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());
         int prevPage = navigationService.getCurrentPage();
         int pageNum = rowIndex / (MAX_PAGE_ROW + 1);
         int rowNum = rowIndex % (MAX_PAGE_ROW + 1);
         if (pageNum != prevPage)
         {
            display.gotoPage(pageNum, false);
         }
         display.gotoRow(rowNum, andEdit);
         selectTransUnit(display.getTransUnitValue(rowNum), andEdit);

         if (pageNum != prevPage)
         {
            display.getTargetCellEditor().cancelEdit();
         }
      }

      @Override
      public void gotoRowInCurrentPage(int rowNum, boolean andEdit)
      {
         display.gotoRow(rowNum, andEdit);
         selectTransUnit(display.getTransUnitValue(rowNum), andEdit);
      }
   };

   private void gotoNextState(Predicate<ContentState> condition)
   {
      display.getTargetCellEditor().cancelEdit();
      int nextRowIndex = navigationService.getNextStateRowIndex(condition);
      Log.info("go to Next State:" + nextRowIndex);
      tableModelHandler.gotoRow(nextRowIndex, true);
   }

   private void gotoPrevState(Predicate<ContentState> condition)
   {
      display.getTargetCellEditor().cancelEdit();
      int prevRowIndex = navigationService.getPreviousStateRowIndex(condition);
      Log.info("go to Prev State:" + prevRowIndex);
      tableModelHandler.gotoRow(prevRowIndex, true);
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
   public void selectTransUnit(final TransUnit transUnit, final boolean andEdit)
   {
      // we want to make sure select transunit always happen first
      scheduler.scheduleEntry(new Command()
      {
         @Override
         public void execute()
         {
            navigationService.updateCurrentPageAndRowIndex(display.getCurrentPage(), display.getSelectedRowNumber());

            display.setTransUnitDetails(transUnit);

            sourceContentsPresenter.setSelectedSource(display.getSelectedRowNumber());
            if (selectedTransUnit == null || !transUnit.getId().equals(selectedTransUnit.getId()))
            {
               selectedTransUnit = transUnit;
               Log.info("SelectedTransUnit: " + selectedTransUnit.getId());
               // Clean the cache when we click the new entry
               eventBus.fireEvent(new TransUnitSelectionEvent(selectedTransUnit));
               display.getTargetCellEditor().savePendingChange(true);
            }
            display.gotoRow(display.getSelectedRowNumber(), andEdit);

         }
      });
   }

   public void gotoCurrentRow()
   {
      tableModelHandler.gotoRow(navigationService.getCurrentRowIndex(), true);
   }

   public void gotoPrevRow(boolean andEdit)
   {
      tableModelHandler.gotoPrevRow(andEdit);

   }

   public void gotoNextRow(boolean andEdit)
   {
      tableModelHandler.gotoNextRow(andEdit);
   }

   public int getSelectedRowIndex()
   {
      return navigationService.getCurrentRowIndex();
   }

   /**
    * Load a document into the editor
    * 
    * @param selectDocId id of the document to select
    */
   private void loadDocument(DocumentId selectDocId)
   {
      if (!selectDocId.equals(documentId))
      {
         documentId = selectDocId;
         initialiseTransUnitList();
      }
   }
}
