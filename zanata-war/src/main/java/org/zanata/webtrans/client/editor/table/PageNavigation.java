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

package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;

import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.client.editor.TransUnitsDataProvider;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.TransUnitNavigationService;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.gen2.logging.shared.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class PageNavigation implements HasPageNavigation
{
   public static final int FIRST_PAGE = 0;
   private final CachingDispatchAsync dispatcher;
   private final TransUnitNavigationService navigationService;
   private final TransUnitsDataProvider dataProvider;
   //tracking variables
   private int pageCount;
   private int totalCount;
   private GetTransUnitActionContext context;
   private ArrayList<TransUnit> currentPageItems = Lists.newArrayList();

   @Inject
   public PageNavigation(CachingDispatchAsync dispatcher, TransUnitNavigationService navigationService, TransUnitsDataProvider dataProvider)
   {
      this.dispatcher = dispatcher;
      this.navigationService = navigationService;
      this.dataProvider = dataProvider;
   }

   public void init(GetTransUnitActionContext context)
   {
      this.context = context;
      requestNavigationIndex(context);
      requestTransUnitsAndUpdatePageIndex(context);
   }

   private void requestTransUnitsAndUpdatePageIndex(GetTransUnitActionContext context)
   {
      Log.info("requesting transUnits: " + toString());
      final int itemPerPage = context.getCount();
      final int offset = context.getOffset();

      dispatcher.execute(GetTransUnitList.newAction(context), new AbstractAsyncCallback<GetTransUnitListResult>()
      {
         @Override
         public void onSuccess(GetTransUnitListResult result)
         {
            Log.info("result unit: " + result.getUnits().size());
            currentPageItems = result.getUnits();
            totalCount = result.getTotalCount();
            pageCount = (int) Math.ceil(totalCount * 1.0 / itemPerPage);

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
      if (navigationService.getCurrentPage() != lastPage())
      {
         GetTransUnitActionContext lastPageContext = context.setOffset(context.getCount() * lastPage()).setTargetTransUnitId(null);
         Log.info("last page context: " + lastPageContext);
         requestTransUnitsAndUpdatePageIndex(lastPageContext);
      }
   }

   @Override
   public void gotoNextPage()
   {
      if (navigationService.getCurrentPage() < lastPage())
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

   private int normalizePageIndex(int pageIndex)
   {
      if (pageIndex < 0)
      {
         return FIRST_PAGE;
      }
      else
      {
         return Math.min(lastPage(), pageIndex);
      }
   }

   public ArrayList<TransUnit> getCurrentPageItems()
   {
      return currentPageItems;
   }

   private int lastPage()
   {
      return pageCount - 1;
   }

   @Override
   public String toString()
   {
      // @formatter:off
        return Objects.toStringHelper(this).
                add("totalCount", totalCount).
                add("pageCount", pageCount).
                add("context", context).
                toString();
        // @formatter:on
   }

   public int getTotalCount()
   {
      return totalCount;
   }
}
