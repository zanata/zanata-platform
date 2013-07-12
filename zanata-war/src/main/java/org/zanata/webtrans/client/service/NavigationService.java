/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.service;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.BookmarkedTextFlowEvent;
import org.zanata.webtrans.client.events.BookmarkedTextFlowEventHandler;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEventHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.InitEditorEvent;
import org.zanata.webtrans.client.events.InitEditorEventHandler;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.RequestPageValidationEvent;
import org.zanata.webtrans.client.events.RequestSelectTableRowEvent;
import org.zanata.webtrans.client.events.RequestSelectTableRowEventHandler;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class NavigationService implements TransUnitUpdatedEventHandler, FindMessageHandler, DocumentSelectionHandler, NavTransUnitHandler, EditorPageSizeChangeEventHandler, BookmarkedTextFlowEventHandler, InitEditorEventHandler, RequestSelectTableRowEventHandler
{
   public static final int FIRST_PAGE = 0;
   public static final int UNDEFINED = -1;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final ModalNavigationStateHolder navigationStateHolder;
   private final GetTransUnitActionContextHolder contextHolder;
   private final UserConfigHolder configHolder;
   private final TableEditorMessages messages;
   private final SinglePageDataModelImpl pageModel;
   private NavigationService.PageDataChangeListener pageDataChangeListener;

   private final History history;

   @Inject
   public NavigationService(EventBus eventBus, CachingDispatchAsync dispatcher, UserConfigHolder configHolder, TableEditorMessages messages, SinglePageDataModelImpl pageModel, ModalNavigationStateHolder navigationStateHolder, GetTransUnitActionContextHolder getTransUnitActionContextHolder, History history)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
      this.messages = messages;
      this.pageModel = pageModel;
      this.navigationStateHolder = navigationStateHolder;
      this.contextHolder = getTransUnitActionContextHolder;
      this.history = history;
      bindHandlers();
   }

   private void bindHandlers()
   {
      eventBus.addHandler(DocumentSelectionEvent.getType(), this);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(EditorPageSizeChangeEvent.TYPE, this);
      eventBus.addHandler(BookmarkedTextFlowEvent.TYPE, this);
      eventBus.addHandler(InitEditorEvent.TYPE, this);
      eventBus.addHandler(RequestSelectTableRowEvent.TYPE, this);
   }

   protected void requestTransUnitsAndUpdatePageIndex(GetTransUnitActionContext actionContext, final boolean needReloadIndex)
   {
      eventBus.fireEvent(LoadingEvent.START_EVENT);

      GetTransUnitList action = GetTransUnitList.newAction(actionContext).setNeedReloadIndex(needReloadIndex);
      Log.info("requesting transUnits: " + action);
      dispatcher.execute(action, new AsyncCallback<GetTransUnitListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("GetTransUnits failure " + caught, caught);
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyLoadFailed()));
            eventBus.fireEvent(LoadingEvent.FINISH_EVENT);
         }

         @Override
         public void onSuccess(GetTransUnitListResult result)
         {
            List<TransUnit> units = result.getUnits();
            Log.info("result size: " + units.size());
            contextHolder.changeOffset(result.getTargetOffset());
            pageModel.setData(units);
            pageDataChangeListener.showDataForCurrentPage(pageModel.getData());

            if (result.getNavigationIndex() != null)
            {
               navigationStateHolder.init(result.getNavigationIndex().getTransIdStateList(), result.getNavigationIndex().getIdIndexList());
               eventBus.fireEvent(new PageCountChangeEvent(navigationStateHolder.getPageCount()));
            }
            navigationStateHolder.updateCurrentPage(result.getTargetPage());

            if (!units.isEmpty())
            {
               TransUnitId selectedId = units.get(result.getGotoRow()).getId();
               navigationStateHolder.updateSelected(selectedId);
               // in case there is pending save (as fuzzy) happening, we do not want to trigger another pending save
               eventBus.fireEvent(new TableRowSelectedEvent(selectedId).setSuppressSavePending(true));
            }
            eventBus.fireEvent(new PageChangeEvent(result.getTargetPage()));
            highlightSearch();

            // run validation on TransUnit and display error message
            eventBus.fireEvent(new RequestPageValidationEvent());
            eventBus.fireEvent(LoadingEvent.FINISH_EVENT);
         }
      });
   }

   private void highlightSearch()
   {
      String findMessage = contextHolder.getContext().getFindMessage();
      if (!Strings.isNullOrEmpty(findMessage))
      {
         pageDataChangeListener.highlightSearch(findMessage);
      }
   }

   public void gotoPage(int pageIndex)
   {
      int page = normalizePageIndex(pageIndex);
      if (page != navigationStateHolder.getCurrentPage())
      {
         GetTransUnitActionContext context = contextHolder.getContext();
         GetTransUnitActionContext newContext = contextHolder.changeOffset(context.getCount() * page).changeTargetTransUnitId(null);
         Log.info("page index: " + page + " page context: " + newContext);
         requestTransUnitsAndUpdatePageIndex(newContext, !configHolder.isAcceptAllStatus());
      }
   }

   private int normalizePageIndex(int pageIndex)
   {
      if (pageIndex <= 0)
      {
         return FIRST_PAGE;
      }
      else
      {
         return Math.min(navigationStateHolder.lastPage(), pageIndex);
      }
   }

   @Override
   public void onNavTransUnit(NavTransUnitEvent event)
   {
      TransUnitId targetId;
      switch (event.getRowType())
      {
         case PrevEntry:
            targetId = navigationStateHolder.getPrevId();
            break;
         case NextEntry:
            targetId = navigationStateHolder.getNextId();
            break;
         case PrevState:
            targetId = navigationStateHolder.getPreviousStateId();
            break;
         case NextState:
            targetId = navigationStateHolder.getNextStateId();
            break;
         case FirstEntry:
            targetId = navigationStateHolder.getFirstId();
            break;
         case LastEntry:
            targetId = navigationStateHolder.getLastId();
            break;
         default:
            Log.warn("ignore unknown navigation type:" + event.getRowType());
            return;
      }
      int targetPage = navigationStateHolder.getTargetPage(targetId);
      Log.info("target page : [" + targetPage + "] target TU id: " + targetId);

      if (navigationStateHolder.getCurrentPage() == targetPage)
      {
         eventBus.fireEvent(new TableRowSelectedEvent(targetId));
      }
      else
      {
         loadPageAndGoToRow(targetPage, targetId);
      }
   }

   private void loadPageAndGoToRow(int pageIndex, TransUnitId transUnitId)
   {
      int page = normalizePageIndex(pageIndex);
      GetTransUnitActionContext context = contextHolder.getContext();
      GetTransUnitActionContext newContext = contextHolder.changeOffset(context.getCount() * page).changeTargetTransUnitId(transUnitId);
      Log.debug("page index: " + page + " page context: " + newContext);
      requestTransUnitsAndUpdatePageIndex(newContext, !configHolder.isAcceptAllStatus());
   }

   @Override
   public void onTransUnitUpdated(TransUnitUpdatedEvent event)
   {
      if(contextHolder.isContextInitialized())
      {
         if (Objects.equal(event.getUpdateInfo().getDocumentId(), contextHolder.getContext().getDocument().getId()))
         {
            TransUnit updatedTU = event.getUpdateInfo().getTransUnit();
            boolean updated = updateDataModel(updatedTU);
            if (updated)
            {
               pageDataChangeListener.refreshRow(updatedTU, event.getEditorClientId(), event.getUpdateType());
            }
         }
      }
   }

   public boolean updateDataModel(TransUnit updatedTU)
   {
      navigationStateHolder.updateState(updatedTU.getId(), updatedTU.getStatus());
      return pageModel.updateIfInCurrentPage(updatedTU);
   }

   @Override
   public void onFindMessage(FindMessageEvent findMessageEvent)
   {
      execute(findMessageEvent);
   }

   @Override
   public void onDocumentSelected(DocumentSelectionEvent documentSelection)
   {
      execute(documentSelection);
   }

   @Override
   public void onBookmarkableTextFlow(BookmarkedTextFlowEvent bookmarkedTextFlowEvent)
   {
      int oldOffset = contextHolder.getContext().getOffset();
      int offset = bookmarkedTextFlowEvent.getOffset();
      if (oldOffset == offset)
      {
         // target text flow is on current page
         eventBus.fireEvent(new TableRowSelectedEvent(bookmarkedTextFlowEvent.getTargetTransUnitId()));
      }
      else
      {
         execute(bookmarkedTextFlowEvent);
      }
   }

   @Override
   public void onPageSizeChange(EditorPageSizeChangeEvent pageSizeChangeEvent)
   {
      execute(pageSizeChangeEvent);
      navigationStateHolder.updatePageSize();
      eventBus.fireEvent(new PageCountChangeEvent(navigationStateHolder.getPageCount()));
   }

   @Override
   public void onInitEditor(InitEditorEvent event)
   {
      requestTransUnitsAndUpdatePageIndex(contextHolder.getContext(), true);
   }

   @Override
   public void onRequestSelectTableRow(RequestSelectTableRowEvent event)
   {
      contextHolder.updateContext(null); // this will ensure
                                         // HistoryEventHandlerService fire
                                         // InitEditorEvent
      HistoryToken token = history.getHistoryToken();
      token.setView(MainView.Editor);
      token.setDocumentPath(event.getDocInfo().getPath() + event.getDocInfo().getName());
      token.clearEditorFilterAndSearch();
      token.setTextFlowId(event.getSelectedId().toString());
      history.newItem(token);
      history.fireCurrentHistoryState();
   }

   public void execute(UpdateContextCommand command)
   {
      GetTransUnitActionContext context = contextHolder.getContext();
      GetTransUnitActionContext newContext = command.updateContext(context);
      Log.debug("old context: " + context);
      Log.debug("new context: " + newContext);
      if (context.needReloadList(newContext))
      {
         boolean needReloadIndex = context.needReloadNavigationIndex(newContext);
         requestTransUnitsAndUpdatePageIndex(newContext, needReloadIndex);
      }
      contextHolder.updateContext(newContext);
   }

   public void selectByRowIndex(int rowIndexOnPage)
   {
      if (pageModel.getCurrentRow() != rowIndexOnPage)
      {
         pageModel.setSelected(rowIndexOnPage);
         contextHolder.changeTargetTransUnitId(pageModel.getSelectedOrNull().getId());
         eventBus.fireEvent(new TransUnitSelectionEvent(getSelectedOrNull()));
         navigationStateHolder.updateSelected(getSelectedOrNull().getId());
      }
   }

   public int getCurrentRowIndexOnPage()
   {
      return pageModel.getCurrentRow();
   }

   public int findRowIndexById(TransUnitId selectedId)
   {
      return pageModel.findIndexById(selectedId);
   }

   public void addPageDataChangeListener(PageDataChangeListener dataChangeListener)
   {
      pageDataChangeListener = dataChangeListener;
   }

   public TransUnit getSelectedOrNull()
   {
      return pageModel.getSelectedOrNull();
   }

   public List<TransUnit> getCurrentPageValues()
   {
      return ImmutableList.copyOf(pageModel.getData());
   }

   public TransUnit getByIdOrNull(TransUnitId transUnitId)
   {
      return pageModel.getByIdOrNull(transUnitId);
   }

   /**
    * for testing only
    * @param context GetTransUnitActionContext
    */
   protected void init(GetTransUnitActionContext context)
   {
      contextHolder.updateContext(context);
      configHolder.setEditorPageSize(context.getCount());
      requestTransUnitsAndUpdatePageIndex(context, true);
   }

   public static interface UpdateContextCommand
   {
      GetTransUnitActionContext updateContext(GetTransUnitActionContext currentContext);
   }

   public static interface PageDataChangeListener
   {
      void showDataForCurrentPage(List<TransUnit> transUnits);

      void refreshRow(TransUnit updatedTransUnit, EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType);

      void highlightSearch(String findMessage);
   }
}
