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

import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.AuthenticationError;
import org.zanata.webtrans.shared.auth.AuthorizationError;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class NavigationController implements HasPageNavigation, TransUnitUpdatedEventHandler, FindMessageHandler, DocumentSelectionHandler
{
   public static final int FIRST_PAGE = 0;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final TransUnitNavigationService navigationService;
   private final TransUnitsDataModel dataModel;
   private final UserConfigHolder configHolder;
   private final TableEditorMessages messages;
   private final TransUnitsPageModel pageModel;
   //tracking variables
   private GetTransUnitActionContext context;

   @Inject
   public NavigationController(EventBus eventBus, CachingDispatchAsync dispatcher, TransUnitNavigationService navigationService, TransUnitsDataModel dataModel, UserConfigHolder configHolder, TableEditorMessages messages, TransUnitsPageModel pageModel)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.navigationService = navigationService;
      this.dataModel = dataModel;
      this.configHolder = configHolder;
      this.messages = messages;
      this.pageModel = pageModel;
      bindHandlers();
   }

   private void bindHandlers()
   {
      eventBus.addHandler(DocumentSelectionEvent.getType(), this);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
   }

   protected void init(GetTransUnitActionContext context)
   {
      this.context = context;
      requestNavigationIndex(context);
      requestTransUnitsAndUpdatePageIndex(context);
   }

   private void requestTransUnitsAndUpdatePageIndex(final GetTransUnitActionContext context)
   {
      this.context = context;
      Log.info("requesting transUnits: " + context);
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
         }

         @Override
         public void onSuccess(GetTransUnitListResult result)
         {
            ArrayList<TransUnit> units = result.getUnits();
            Log.info("result unit: " + units.size());
            pageModel.setData(units);
//            dataModel.setList(units);
//            dataModel.refresh(); // force the display to re-render

            // default values
            int gotoRow = 0;
            int currentPageIndex = offset / itemPerPage;

            // if we need to scroll to a particular item
            if (result.getGotoRow() != -1)
            {
               currentPageIndex = result.getGotoRow() / itemPerPage;
               gotoRow = result.getGotoRow() % itemPerPage;
            }
            navigationService.updateCurrentPageAndRowIndex(currentPageIndex, gotoRow);
//            TransUnitId goToRowId = result.getUnits().get(gotoRow).getId();
            pageModel.setSelected(gotoRow);
//            dataModel.selectById(goToRowId);
            eventBus.fireEvent(new PageChangeEvent(navigationService.getCurrentPage()));
         }
      });
   }

   private void requestNavigationIndex(GetTransUnitActionContext context)
   {
      this.context = context;
      final int itemPerPage = context.getCount();
      dispatcher.execute(GetTransUnitsNavigation.newAction(context), new AbstractAsyncCallback<GetTransUnitsNavigationResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsNavigationResult result)
         {
            navigationService.init(result.getTransIdStateList(), result.getIdIndexList(), itemPerPage);
            eventBus.fireEvent(new PageCountChangeEvent(navigationService.getPageCount()));
         }
      });
   }

   @Override
   public void gotoFirstPage()
   {
      if (navigationService.getCurrentPage() != FIRST_PAGE)
      {
         GetTransUnitActionContext firstPageContext = context.setOffset(0).setTargetTransUnitId(null);
         Log.info("first page context: " + firstPageContext);
         requestTransUnitsAndUpdatePageIndex(firstPageContext);
      }
   }

   @Override
   public void gotoLastPage()
   {
      if (navigationService.getCurrentPage() != navigationService.lastPage())
      {
         GetTransUnitActionContext lastPageContext = context.setOffset(context.getCount() * navigationService.lastPage()).setTargetTransUnitId(null);
         Log.info("last page context: " + lastPageContext);
         requestTransUnitsAndUpdatePageIndex(lastPageContext);
      }
   }

   @Override
   public void gotoNextPage()
   {
      if (navigationService.getCurrentPage() < navigationService.lastPage())
      {
         int nextPage = navigationService.getCurrentPage() + 1;

         GetTransUnitActionContext nextPageContext = context.setOffset(context.getCount() * nextPage).setTargetTransUnitId(null);
         Log.info("next page context: " + nextPageContext);
         requestTransUnitsAndUpdatePageIndex(nextPageContext);
      }
   }

   @Override
   public void gotoPreviousPage()
   {
      if (navigationService.getCurrentPage() > FIRST_PAGE)
      {
         int previousPage = navigationService.getCurrentPage() - 1;
         GetTransUnitActionContext previousPageContext = context.setOffset(context.getCount() * previousPage).setTargetTransUnitId(null);
         Log.info("previous page context: " + previousPageContext);
         requestTransUnitsAndUpdatePageIndex(previousPageContext);
      }
   }

   @Override
   public void gotoPage(int pageIndex, boolean forceReload)
   {
      int page = normalizePageIndex(pageIndex);
      if (page != navigationService.getCurrentPage() || forceReload)
      {
         GetTransUnitActionContext newContext = context.setOffset(context.getCount() * page).setTargetTransUnitId(null);
         Log.info("page index: " + pageIndex + " page context: " + newContext);
         requestTransUnitsAndUpdatePageIndex(newContext);
      }
   }

   private void loadPageAndGoToRow(int pageIndex, TransUnitId transUnitId)
   {
      int page = normalizePageIndex(pageIndex);
      GetTransUnitActionContext newContext = context.setOffset(context.getCount() * page).setTargetTransUnitId(transUnitId);
      Log.info("page index: " + pageIndex + " page context: " + newContext);
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

   public TransUnitsDataModel getDataModel()
   {
      return dataModel;
   }

   //TODO clean up this class dependency mess
   public void navigateTo(NavTransUnitEvent.NavigationType navigationType)
   {
      int rowIndex;
      switch (navigationType)
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
            Log.warn("ignore unknown navigation type:" + navigationType);
            return;
      }
      int targetPage = navigationService.getTargetPage(rowIndex);
      TransUnitId targetTransUnitId = navigationService.getTargetTransUnitId(rowIndex);
      Log.info("target page : [" + targetPage + "] target TU id: " + targetTransUnitId);

      if (navigationService.getCurrentPage() == targetPage)
      {
         dataModel.selectById(targetTransUnitId);
         navigationService.updateCurrentPageAndRowIndex(targetPage, dataModel.getIndexOnPage(targetTransUnitId));
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
         dataModel.update(updatedTU);
      }
   }

   @Override
   public void onFindMessage(FindMessageEvent event)
   {
      // TODO modal navigation disabled if there's findMessage. turn FindMessageEvent into UpdateContextCommand like the rest.
      String findMessage = event.getMessage();
      context = context.setFindMessage(findMessage);
      if (Strings.isNullOrEmpty(findMessage))
      {
         init(context);
         eventBus.fireEvent(new EnableModalNavigationEvent(true));
      }
      else
      {
         eventBus.fireEvent(new EnableModalNavigationEvent(false));
         requestTransUnitsAndUpdatePageIndex(context);
      }
   }

   @Override
   public void onDocumentSelected(DocumentSelectionEvent documentSelection)
   {
      execute(documentSelection);
   }

   public void execute(UpdateContextCommand command)
   {
      if (context == null)
      {
         Preconditions.checkState(command instanceof DocumentSelectionEvent, "no existing context available. Must select document first.");
         DocumentId documentId = ((DocumentSelectionEvent) command).getDocumentId();
         init(GetTransUnitActionContext.of(documentId));
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
      }
   }

   private GetTransUnitActionContext setTargetTransUnitIdIfApplicable(GetTransUnitActionContext context)
   {
      TransUnit selected = dataModel.getSelectedOrNull();
      return selected != null ? context.setTargetTransUnitId(selected.getId()) : context;
   }

   public static interface UpdateContextCommand
   {
      GetTransUnitActionContext updateContext(GetTransUnitActionContext currentContext);
   }

   public TransUnitsPageModel getPageModel()
   {
      return pageModel;
   }
}
