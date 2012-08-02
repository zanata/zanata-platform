/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Predicate;
import com.google.inject.Singleton;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/

@Singleton
//TODO after retiring TableEditorPresenter remove unused methods and maybe rename it to something else (NavigationStateHolder?) and get the NavigationController to be named after Service.
class TransUnitNavigationService
{
   private Map<Long, ContentState> idAndStateMap;
   private ArrayList<Long> idIndexList;

   private int pageSize;
   private int rowIndexInDocument = 0;
   private int curPage = 0;
   private int totalCount;
   private int pageCount;

   public void init(Map<Long, ContentState> transIdStateMap, ArrayList<Long> idIndexList, int pageSize)
   {
      this.idAndStateMap = transIdStateMap;
      this.idIndexList = idIndexList;
      this.pageSize = pageSize;
      totalCount = idIndexList.size();
      pageCount = (int) Math.ceil(totalCount * 1.0 / pageSize);
   }

   public void updateState(Long id, ContentState newState)
   {
      idAndStateMap.put(id, newState);
   }

   public int getNextStateRowIndex(Predicate<ContentState> condition)
   {
      for (int i = rowIndexInDocument + 1; i <= maxRowIndex(); i++)
      {
         ContentState contentState = idAndStateMap.get(idIndexList.get(i));
         if (condition.apply(contentState))
         {
            return i;
         }
      }
      return rowIndexInDocument;
   }

   public int maxRowIndex()
   {
      return totalCount - 1;
   }

   public int getPreviousStateRowIndex(Predicate<ContentState> condition)
   {
      for (int i = rowIndexInDocument - 1; i >= 0; i--)
      {
         ContentState contentState = idAndStateMap.get(idIndexList.get(i));
         if (condition.apply(contentState))
         {
            return i;
         }
      }
      return rowIndexInDocument;
   }

   public Integer getRowIndex(TransUnit tu, boolean isFiltering, List<TransUnit> rowValues)
   {
      if (tu == null)
      {
         return null;
      }
      if (!isFiltering)
      {
         return tu.getRowIndex();
      }
      else
      {
         TransUnitId transUnitId = tu.getId();
         int n = 0;
         for (TransUnit transUnit : rowValues)
         {
            if (transUnitId.equals(transUnit.getId()))
            {
               return n + (curPage * pageSize);
            }
            n++;
         }
      }
      return null;
   }

   public Integer getRowNumber(TransUnit tu, List<TransUnit> rowValues)
   {
      if (tu == null)
      {
         return null;
      }
      else
      {
         TransUnitId transUnitId = tu.getId();
         int n = 0;
         for (TransUnit transUnit : rowValues)
         {
            if (transUnitId.equals(transUnit.getId()))
            {
               return n;
            }
            n++;
         }
      }
      return null;
   }

   public void updateCurrentPageAndRowIndex(int curPage, int selectedRow)
   {
      this.curPage = curPage;
      rowIndexInDocument = this.curPage * pageSize + selectedRow;
      Log.debug("update current page:" + curPage + ", current row index:" + rowIndexInDocument);
   }

   public int getCurrentPage()
   {
      return curPage;
   }

   public int getCurrentRowIndex()
   {
      return rowIndexInDocument;
   }

   public int getCurrentRowNumber()
   {
      return rowIndexInDocument - (curPage * pageSize);
   }

   public int getNextRowIndex()
   {
      return Math.min(rowIndexInDocument + 1, maxRowIndex());
   }

   public int getPrevRowIndex()
   {
      return Math.max(rowIndexInDocument - 1, 0);
   }

   public int getTargetPage(int targetIndex)
   {
      return targetIndex / pageSize;
   }

   public TransUnitId getTargetTransUnitId(int rowIndex)
   {
      return new TransUnitId(idIndexList.get(rowIndex));
   }

   public int lastPage()
   {
      return pageCount - 1;
   }

   public int getPageCount()
   {
      return pageCount;
   }
}
