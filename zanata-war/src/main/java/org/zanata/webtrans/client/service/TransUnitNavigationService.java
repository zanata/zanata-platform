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

import com.google.common.base.Predicate;
import com.google.inject.Singleton;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/

@Singleton
public class TransUnitNavigationService
{
   public static final Predicate<ContentState> FUZZY_OR_NEW_PREDICATE = new Predicate<ContentState>()
   {
      @Override
      public boolean apply(ContentState contentState)
      {
         return contentState == ContentState.New || contentState == ContentState.NeedReview;
      }
   };
   public static final Predicate<ContentState> FUZZY_PREDICATE = new Predicate<ContentState>()
   {
      @Override
      public boolean apply(ContentState contentState)
      {
         return contentState == ContentState.NeedReview;
      }
   };
   public static final Predicate<ContentState> NEW_PREDICATE = new Predicate<ContentState>()
   {
      @Override
      public boolean apply(ContentState contentState)
      {
         return contentState == ContentState.New;
      }
   };

   private Map<Long, ContentState> transIdStateList;
   private ArrayList<Long> idIndexList;

   private int pageSize;
   private int curRowIndex;
   private int curPage;

   public void init(Map<Long, ContentState> transIdStateList, ArrayList<Long> idIndexList)
   {
      this.transIdStateList = transIdStateList;
      this.idIndexList = idIndexList;
   }

   public void updateMap(Long id, ContentState newState)
   {
      transIdStateList.put(id, newState);
   }

   public int getNextStateRowIndex(Predicate<ContentState> condition)
   {
      if (curRowIndex >= idIndexList.size() - 1)
      {
         return curRowIndex;
      }

      for (int i = curRowIndex + 1; i <= idIndexList.size() - 1; i++)
      {
         ContentState contentState = transIdStateList.get(idIndexList.get(i));
         if (condition.apply(contentState))
         {
            return i;
         }
      }
      return curRowIndex;
   }

   public int getPreviousStateRowIndex(Predicate<ContentState> condition)
   {
      if (curRowIndex == 0)
      {
         return curRowIndex;
      }

      for (int i = curRowIndex - 1; i >= 0; i--)
      {
         ContentState contentState = transIdStateList.get(idIndexList.get(i));
         if (condition.apply(contentState))
         {
            return i;
         }
      }
      return curRowIndex;
   }

   public Integer getRowIndex(TransUnit tu, boolean isFiltering, List<TransUnit> rowValues)
   {
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
               int row = n + (curPage * pageSize);
               return row;
            }
            n++;
         }
      }
      return null;
   }

   public void updateCurrentPageAndRowIndex(int curPage, int selectedRow)
   {
      this.curPage = curPage;
      curRowIndex = this.curPage * pageSize + selectedRow;
   }

   public int getCurrentPage()
   {
      return curPage;
   }

   public int getCurrentRowIndex()
   {
      return curRowIndex;
   }

   public int getNextRowIndex()
   {
      return curRowIndex + 1;
   }

   public int getPrevRowIndex()
   {
      return curRowIndex - 1;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }
}
