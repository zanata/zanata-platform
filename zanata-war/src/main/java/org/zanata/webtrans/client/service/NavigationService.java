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
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEventHandler;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
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
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class NavigationService implements TransUnitUpdatedEventHandler, FindMessageHandler, DocumentSelectionHandler, NavTransUnitHandler, EditorPageSizeChangeEventHandler
{
   public static final int FIRST_PAGE = 0;
   public static final int UNSELECTED = -1;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final ModalNavigationStateHolder navigationStateHolder;
   private final UserConfigHolder configHolder;
   private final TableEditorMessages messages;
   private final SinglePageDataModelImpl pageModel;
   private NavigationService.PageDataChangeListener pageDataChangeListener;

   //tracking variables
   private GetTransUnitActionContext context;
   private boolean isLoadingTU = false;
   private boolean isLoadingIndex =false;

   @Inject
   public NavigationService(EventBus eventBus, CachingDispatchAsync dispatcher, UserConfigHolder configHolder, TableEditorMessages messages)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
      this.messages = messages;
      this.pageModel = new SinglePageDataModelImpl();
      this.navigationStateHolder = new ModalNavigationStateHolder();
      bindHandlers();
   }

   private void bindHandlers()
   {
      eventBus.addHandler(DocumentSelectionEvent.getType(), this);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(EditorPageSizeChangeEvent.TYPE, this);
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
            context = context.changeOffset(result.getTargetOffset());
            pageModel.setData(units);
            pageDataChangeListener.showDataForCurrentPage(pageModel.getData());

            if (result.getNavigationIndex() != null)
            {
               navigationStateHolder.init(result.getNavigationIndex().getTransIdStateList(), result.getNavigationIndex().getIdIndexList(), context.getCount());
               eventBus.fireEvent(new PageCountChangeEvent(navigationStateHolder.getPageCount()));
            }
            navigationStateHolder.updateCurrentPage(result.getTargetPage());

            if (!units.isEmpty())
            {
               // in case there is pending save (as fuzzy) happening, we do not want to trigger another pending save
               eventBus.fireEvent(new TableRowSelectedEvent(units.get(result.getGotoRow()).getId()).setSuppressSavePending(true));
            }
            eventBus.fireEvent(new PageChangeEvent(result.getTargetPage()));
            highlightSearch();
            eventBus.fireEvent(LoadingEvent.FINISH_EVENT);
         }
      });
   }

   private void highlightSearch()
   {
      if (!Strings.isNullOrEmpty(context.getFindMessage()))
      {
         pageDataChangeListener.highlightSearch(context.getFindMessage());
      }
   }

   public void gotoPage(int pageIndex)
   {
      int page = normalizePageIndex(pageIndex);
      if (page != navigationStateHolder.getCurrentPage())
      {
         GetTransUnitActionContext newContext = context.changeOffset(context.getCount() * page).changeTargetTransUnitId(null);
         Log.info("page index: " + page + " page context: " + newContext);
         requestTransUnitsAndUpdatePageIndex(newContext, false);
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
      int rowIndex;
      switch (event.getRowType())
      {
         case PrevEntry:
            rowIndex = navigationStateHolder.getPrevRowIndex();
            break;
         case NextEntry:
            rowIndex = navigationStateHolder.getNextRowIndex();
            break;
         case PrevState:
            rowIndex = navigationStateHolder.getPreviousStateRowIndex(configHolder.getContentStatePredicate());
            break;
         case NextState:
            rowIndex = navigationStateHolder.getNextStateRowIndex(configHolder.getContentStatePredicate());
            break;
         case FirstEntry:
            rowIndex = 0;
            break;
         case LastEntry:
            rowIndex = navigationStateHolder.maxRowIndex();
            break;
         default:
            Log.warn("ignore unknown navigation type:" + event.getRowType());
            return;
      }
      int targetPage = navigationStateHolder.getTargetPage(rowIndex);
      TransUnitId targetTransUnitId = navigationStateHolder.getTargetTransUnitId(rowIndex);
      Log.info("target page : [" + targetPage + "] target TU id: " + targetTransUnitId + " rowIndex: " + rowIndex);

      if (navigationStateHolder.getCurrentPage() == targetPage)
      {
         eventBus.fireEvent(new TableRowSelectedEvent(targetTransUnitId));
      }
      else
      {
         loadPageAndGoToRow(targetPage, targetTransUnitId);
      }
   }

   private void loadPageAndGoToRow(int pageIndex, TransUnitId transUnitId)
   {
      int page = normalizePageIndex(pageIndex);
      GetTransUnitActionContext newContext = context.changeOffset(context.getCount() * page).changeTargetTransUnitId(transUnitId);
      Log.debug("page index: " + page + " page context: " + newContext);
      requestTransUnitsAndUpdatePageIndex(newContext, false);
   }

   @Override
   public void onTransUnitUpdated(TransUnitUpdatedEvent event)
   {
      if (Objects.equal(event.getUpdateInfo().getDocumentId(), context.getDocumentId()))
      {
         TransUnit updatedTU = event.getUpdateInfo().getTransUnit();
         boolean updated = updateDataModel(updatedTU);
         if (updated)
         {
            pageDataChangeListener.refreshRow(updatedTU, event.getEditorClientId(), event.getUpdateType());
         }
      }
   }

   public boolean updateDataModel(TransUnit updatedTU)
   {
      navigationStateHolder.updateState(updatedTU.getId().getId(), updatedTU.getStatus());
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
   public void onPageSizeChange(EditorPageSizeChangeEvent pageSizeChangeEvent)
   {
      execute(pageSizeChangeEvent);
      navigationStateHolder.updatePageSize(pageSizeChangeEvent.getPageSize());
      eventBus.fireEvent(new PageCountChangeEvent(navigationStateHolder.getPageCount()));
   }

   public void execute(UpdateContextCommand command)
   {
      if (context == null)
      {
         Preconditions.checkState(command instanceof DocumentSelectionEvent, "no existing context available. Must select document first.");
         DocumentSelectionEvent documentSelectionEvent = (DocumentSelectionEvent) command;
         DocumentId documentId = documentSelectionEvent.getDocumentId();
         // @formatter:off
         context = new GetTransUnitActionContext(documentId)
               .changeCount(configHolder.getEditorPageSize())
               .changeFindMessage(documentSelectionEvent.getFindMessage())
               .changeFilterNeedReview(configHolder.getState().isFilterByNeedReview())
               .changeFilterTranslated(configHolder.getState().isFilterByTranslated())
               .changeFilterUntranslated(configHolder.getState().isFilterByUntranslated());
         requestTransUnitsAndUpdatePageIndex(context, true);
         // @formatter:on
      }
      else
      {
         GetTransUnitActionContext newContext = command.updateContext(context);
         if (context.needReloadList(newContext))
         {
            boolean needReloadIndex = context.needReloadNavigationIndex(newContext);
            requestTransUnitsAndUpdatePageIndex(newContext, needReloadIndex);
         }
         this.context = newContext;
      }
   }

   public void selectByRowIndex(int rowIndexOnPage)
   {
      if (pageModel.getCurrentRow() != rowIndexOnPage)
      {
         pageModel.setSelected(rowIndexOnPage);
         context = context.changeTargetTransUnitId(pageModel.getSelectedOrNull().getId());
         eventBus.fireEvent(new TransUnitSelectionEvent(getSelectedOrNull()));
         navigationStateHolder.updateRowIndexInDocument(rowIndexOnPage);
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
    * for testing only.
    * @return navigation state holder
    */
   protected ModalNavigationStateHolder getNavigationStateHolder()
   {
      return navigationStateHolder;
   }

   /**
    * for testing only
    * @param context GetTransUnitActionContext
    */
   protected void init(GetTransUnitActionContext context)
   {
      this.context = context;
      requestTransUnitsAndUpdatePageIndex(context, true);
   }

   /**
    * for testing only.
    * @return page model
    */
   protected SinglePageDataModelImpl getPageModel()
   {
      return pageModel;
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
