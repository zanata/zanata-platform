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

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.PageSizeChangeEvent;
import org.zanata.webtrans.client.events.PageSizeChangeEventHandler;
import org.zanata.webtrans.client.events.ReloadPageEvent;
import org.zanata.webtrans.client.events.ReloadPageEventHandler;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class NavigationController implements TransUnitUpdatedEventHandler, FindMessageHandler, DocumentSelectionHandler, NavTransUnitHandler, PageSizeChangeEventHandler, ReloadPageEventHandler
{
   public static final int FIRST_PAGE = 0;
   public static final int UNSELECTED = -1;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final TransUnitNavigationService navigationService;
   private final UserConfigHolder configHolder;
   private final TableEditorMessages messages;
   private final SinglePageDataModelImpl pageModel;
   private NavigationController.PageDataChangeListener pageDataChangeListener;

   //tracking variables
   private GetTransUnitActionContext context;
   private String findMessage;
   private boolean isLoadingTU = false;
   private boolean isLoadingIndex =false;

   @Inject
   public NavigationController(EventBus eventBus, CachingDispatchAsync dispatcher, TransUnitNavigationService navigationService, UserConfigHolder configHolder, TableEditorMessages messages)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
      this.messages = messages;
      this.pageModel = new SinglePageDataModelImpl();
      this.navigationService = navigationService;
      bindHandlers();
   }

   private void bindHandlers()
   {
      eventBus.addHandler(DocumentSelectionEvent.getType(), this);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(PageSizeChangeEvent.TYPE, this);
      eventBus.addHandler(ReloadPageEvent.TYPE, this);
   }

   protected void init(GetTransUnitActionContext context)
   {
      this.context = context;
      requestNavigationIndex(context);
      requestTransUnitsAndUpdatePageIndex(context);
   }

   private void requestTransUnitsAndUpdatePageIndex(final GetTransUnitActionContext context)
   {
      Log.info("requesting transUnits: " + context);
      isLoadingTU = true;
      startLoading();
      final int itemPerPage = context.getCount();
      final int offset = context.getOffset();

      dispatcher.execute(GetTransUnitList.newAction(context), new AsyncCallback<GetTransUnitListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            if (caught instanceof AuthenticationError)
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyNotLoggedIn()));
            }
            else
            {
               Log.error("GetTransUnits failure " + caught, caught);
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyLoadFailed()));
            }
            isLoadingTU = false;
            finishLoading();
         }

         @Override
         public void onSuccess(GetTransUnitListResult result)
         {
            ArrayList<TransUnit> units = result.getUnits();
            Log.info("result unit: " + units.size());
            pageModel.setData(units);
            pageDataChangeListener.showDataForCurrentPage(pageModel.getData());

            // default values
            int gotoRow = 0;
            int currentPageIndex = offset / itemPerPage;

            // if we need to scroll to a particular item
            if (result.getGotoRow() != -1)
            {
               currentPageIndex = result.getGotoRow() / itemPerPage;
               gotoRow = result.getGotoRow() % itemPerPage;
            }
            navigationService.updateCurrentPage(currentPageIndex);

            if (!units.isEmpty())
            {
               eventBus.fireEvent(new TableRowSelectedEvent(units.get(gotoRow).getId()));
            }
            eventBus.fireEvent(new PageChangeEvent(navigationService.getCurrentPage()));
            isLoadingTU = false;
            finishLoading();
         }
      });
   }

   private void startLoading()
   {
      if (isLoadingTU || isLoadingIndex)
      {
         eventBus.fireEvent(LoadingEvent.START_EVENT);
      }
   }

   private void finishLoading()
   {
      // we only send finish event when all loading are finished
      if (!isLoadingTU && !isLoadingIndex)
      {
         eventBus.fireEvent(LoadingEvent.FINISH_EVENT);
      }
   }

   private void requestNavigationIndex(GetTransUnitActionContext context)
   {
      Log.info("requesting navigation index: " + context);
      isLoadingIndex = true;
      startLoading();
      final int itemPerPage = context.getCount();
      dispatcher.execute(GetTransUnitsNavigation.newAction(context), new AbstractAsyncCallback<GetTransUnitsNavigationResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsNavigationResult result)
         {
            navigationService.init(result.getTransIdStateList(), result.getIdIndexList(), itemPerPage);
            eventBus.fireEvent(new PageCountChangeEvent(navigationService.getPageCount()));
            isLoadingIndex = false;
            finishLoading();
         }
      });
   }

   public void gotoPage(int pageIndex)
   {
      int page = normalizePageIndex(pageIndex);
      if (page != navigationService.getCurrentPage())
      {
         GetTransUnitActionContext newContext = context.changeOffset(context.getCount() * page).changeTargetTransUnitId(null);
         Log.info("page index: " + page + " page context: " + newContext);
         requestTransUnitsAndUpdatePageIndex(newContext);
      }
   }

   private void loadPageAndGoToRow(int pageIndex, TransUnitId transUnitId)
   {
      int page = normalizePageIndex(pageIndex);
      GetTransUnitActionContext newContext = context.changeOffset(context.getCount() * page).changeTargetTransUnitId(transUnitId);
      Log.debug("page index: " + page + " page context: " + newContext);
      requestTransUnitsAndUpdatePageIndex(newContext);
   }

   private int normalizePageIndex(int pageIndex)
   {
      if (pageIndex <= 0)
      {
         return FIRST_PAGE;
      }
      else
      {
         return Math.min(navigationService.lastPage(), pageIndex);
      }
   }

   @Override
   public void onNavTransUnit(NavTransUnitEvent event)
   {
      int rowIndex;
      switch (event.getRowType())
      {
         case PrevEntry:
            rowIndex = navigationService.getPrevRowIndex();
            break;
         case NextEntry:
            rowIndex = navigationService.getNextRowIndex();
            break;
         case PrevState:
            rowIndex = navigationService.getPreviousStateRowIndex(configHolder.getContentStatePredicate());
            break;
         case NextState:
            rowIndex = navigationService.getNextStateRowIndex(configHolder.getContentStatePredicate());
            break;
         case FirstEntry:
            rowIndex = 0;
            break;
         case LastEntry:
            rowIndex = navigationService.maxRowIndex();
            break;
         default:
            Log.warn("ignore unknown navigation type:" + event.getRowType());
            return;
      }
      int targetPage = navigationService.getTargetPage(rowIndex);
      TransUnitId targetTransUnitId = navigationService.getTargetTransUnitId(rowIndex);
      Log.info("target page : [" + targetPage + "] target TU id: " + targetTransUnitId + " rowIndex: " + rowIndex);

      if (navigationService.getCurrentPage() == targetPage)
      {
         eventBus.fireEvent(new TableRowSelectedEvent(targetTransUnitId));
      }
      else
      {
         loadPageAndGoToRow(targetPage, targetTransUnitId);
      }
   }

   @Override
   public void onTransUnitUpdated(TransUnitUpdatedEvent event)
   {
      if (Objects.equal(event.getUpdateInfo().getDocumentId(), context.getDocumentId()))
      {
         TransUnit updatedTU = event.getUpdateInfo().getTransUnit();
         navigationService.updateState(updatedTU.getId().getId(), updatedTU.getStatus());
         boolean updated = pageModel.updateIfInCurrentPage(updatedTU);
         if (updated)
         {
            pageDataChangeListener.refreshView(updatedTU, event.getEditorClientId(), event.getUpdateType());
         }
      }
   }

   @Override
   public void onFindMessage(FindMessageEvent findMessageEvent)
   {
      findMessage = findMessageEvent.getMessage();
      // context may be null if loading from bookmark (document is not yet loaded)
      if (context == null)
      {
         return;
      }
      execute(findMessageEvent);
   }

   @Override
   public void onDocumentSelected(DocumentSelectionEvent documentSelection)
   {
      execute(documentSelection);
   }

   @Override
   public void onPageSizeChange(PageSizeChangeEvent pageSizeChangeEvent)
   {
      execute(pageSizeChangeEvent);
      navigationService.updatePageSize(pageSizeChangeEvent.getPageSize());
      eventBus.fireEvent(new PageCountChangeEvent(navigationService.getPageCount()));
   }

   @Override
   public void onReloadPage(ReloadPageEvent event)
   {
      Log.info("refreshing page");
      isLoadingTU = true;
      startLoading();
      pageDataChangeListener.showDataForCurrentPage(pageModel.getData());
      isLoadingTU = false;
      finishLoading();
   }

   public void execute(UpdateContextCommand command)
   {
      if (context == null)
      {
         Preconditions.checkState(command instanceof DocumentSelectionEvent, "no existing context available. Must select document first.");
         DocumentId documentId = ((DocumentSelectionEvent) command).getDocumentId();
         init(new GetTransUnitActionContext(documentId).changeCount(configHolder.getPageSize()).changeFindMessage(findMessage));
      }
      else
      {
         GetTransUnitActionContext newContext = command.updateContext(context);
         if (context.needReloadList(newContext))
         {
            requestTransUnitsAndUpdatePageIndex(setTargetTransUnitIdIfApplicable(newContext));
         }
         if (context.needReloadNavigationIndex(newContext))
         {
            requestNavigationIndex(newContext);
         }
         this.context = newContext;
      }
   }

   private GetTransUnitActionContext setTargetTransUnitIdIfApplicable(GetTransUnitActionContext context)
   {
      TransUnit selected = pageModel.getSelectedOrNull();
      return selected != null ? context.changeTargetTransUnitId(selected.getId()) : context;
   }

   public void selectByRowIndex(int rowIndexOnPage)
   {
      if (pageModel.getCurrentRow() != rowIndexOnPage)
      {
         pageModel.setSelected(rowIndexOnPage);
         eventBus.fireEvent(new TransUnitSelectionEvent(getSelectedOrNull()));
         navigationService.updateRowIndexInDocument(rowIndexOnPage);
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

   public static interface UpdateContextCommand
   {
      GetTransUnitActionContext updateContext(GetTransUnitActionContext currentContext);
   }

   public static interface PageDataChangeListener
   {
      void showDataForCurrentPage(List<TransUnit> transUnits);

      void refreshView(TransUnit updatedTransUnit, EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType);
   }
}
