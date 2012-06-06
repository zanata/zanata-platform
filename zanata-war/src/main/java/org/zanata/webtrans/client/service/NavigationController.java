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
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class NavigationController implements HasPageNavigation
{
   public static final int FIRST_PAGE = 0;
   private final CachingDispatchAsync dispatcher;
   private final TransUnitNavigationService navigationService;
   private final TransUnitsDataModel dataModel;
   private final UserConfigHolder configHolder;
   //tracking variables
   private GetTransUnitActionContext context;

   @Inject
   public NavigationController(CachingDispatchAsync dispatcher, TransUnitNavigationService navigationService, TransUnitsDataModel dataModel, UserConfigHolder configHolder)
   {
      this.dispatcher = dispatcher;
      this.navigationService = navigationService;
      this.dataModel = dataModel;
      this.configHolder = configHolder;
   }

   public void init(GetTransUnitActionContext context)
   {
      this.context = context;
      requestNavigationIndex(context);
      requestTransUnitsAndUpdatePageIndex(context);
   }

   private void requestTransUnitsAndUpdatePageIndex(GetTransUnitActionContext context)
   {
      Log.info("requesting transUnits: " + context);
      final int itemPerPage = context.getCount();
      final int offset = context.getOffset();

      dispatcher.execute(GetTransUnitList.newAction(context), new AbstractAsyncCallback<GetTransUnitListResult>()
      {
         @Override
         public void onSuccess(GetTransUnitListResult result)
         {
            ArrayList<TransUnit> units = result.getUnits();
            Log.info("result unit: " + units.size());
            dataModel.setList(units);
            dataModel.refresh(); // force the display to re-render

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
            dataModel.selectByRowNumber(gotoRow);
         }
      });
   }

   private void requestNavigationIndex(GetTransUnitActionContext context)
   {
      final int itemPerPage = context.getCount();
      dispatcher.execute(GetTransUnitsNavigation.newAction(context), new AbstractAsyncCallback<GetTransUnitsNavigationResult>()
      {
         @Override
         public void onSuccess(GetTransUnitsNavigationResult result)
         {
            navigationService.init(result.getTransIdStateList(), result.getIdIndexList(), itemPerPage);
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
         Log.info(pageIndex + " page context: " + newContext);
         requestTransUnitsAndUpdatePageIndex(newContext);
      }
   }

   public void loadPageAndGoToRow(int pageIndex, TransUnitId transUnitId)
   {
      int page = normalizePageIndex(pageIndex);
      GetTransUnitActionContext newContext = context.setOffset(context.getCount() * page).setTargetTransUnitId(transUnitId);
      Log.info(pageIndex + " page context: " + newContext);
      requestTransUnitsAndUpdatePageIndex(newContext);
   }

   private int normalizePageIndex(int pageIndex)
   {
      if (pageIndex < 0)
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
      int rowIndex = 0;
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
            rowIndex = navigationService.lastPage();
            break;
      }
      int targetPage = navigationService.getTargetPage(rowIndex);
      TransUnitId targetTransUnitId = navigationService.getTargetTransUnitId(rowIndex);
      Log.info("target page : [" + targetPage + "] target TU id: " + targetTransUnitId);

      if (navigationService.getCurrentPage() == targetPage)
      {
         dataModel.selectById(targetTransUnitId);
      }
      loadPageAndGoToRow(targetPage, targetTransUnitId);
   }
}
