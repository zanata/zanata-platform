/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;
import java.util.Map;

import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.base.Predicate;
import com.google.inject.Singleton;

@Singleton
public class TransUnitsModel implements EditRowCallback
{
   public static final int INIT_INDEX = 0;
   private static final Predicate<ContentState> FUZZY_OR_NEW_PREDICATE = new Predicate<ContentState>()
   {
      @Override
      public boolean apply(ContentState contentState)
      {
         return contentState == ContentState.New || contentState == ContentState.NeedReview;
      }
   };
   private static final Predicate<ContentState> FUZZY_PREDICATE = new Predicate<ContentState>()
   {
      @Override
      public boolean apply(ContentState contentState)
      {
         return contentState == ContentState.NeedReview;
      }
   };
   private static final Predicate<ContentState> NEW_PREDICATE = new Predicate<ContentState>()
   {
      @Override
      public boolean apply(ContentState contentState)
      {
         return contentState == ContentState.New;
      }
   };

   private int currentIndex = INIT_INDEX;

   private Map<Long, ContentState> transIdStateList;
   private ArrayList<Long> idIndexList;

   public void init(Map<Long, ContentState> transIdStateList, ArrayList<Long> idIndexList)
   {
      this.transIdStateList = transIdStateList;
      this.idIndexList = idIndexList;

      resetAllIndexes();
   }

   private void resetAllIndexes()
   {
      currentIndex = INIT_INDEX;
   }

   @Override
   public void gotoNextRow()
   {
      if (currentIndex < idIndexList.size() - 1)
      {
         currentIndex++;
      }
   }

   @Override
   public void gotoPrevRow()
   {
      if (currentIndex > 0)
      {
         currentIndex--;
      }
   }

   @Override
   public void gotoFirstRow()
   {
      currentIndex = 0;
   }

   @Override
   public void gotoLastRow()
   {
      currentIndex = idIndexList.size() - 1;
   }

   @Override
   public void gotoNextFuzzyNewRow()
   {
      moveForwardAndFind(FUZZY_OR_NEW_PREDICATE);
   }

   @Override
   public void gotoPrevFuzzyNewRow()
   {
      moveBackwardAndFind(FUZZY_OR_NEW_PREDICATE);
   }

   @Override
   public void gotoNextFuzzyRow()
   {
      moveForwardAndFind(FUZZY_PREDICATE);
   }

   @Override
   public void gotoPrevFuzzyRow()
   {
      moveBackwardAndFind(FUZZY_PREDICATE);
   }

   @Override
   public void gotoNextNewRow()
   {
      moveForwardAndFind(NEW_PREDICATE);
   }

   @Override
   public void gotoCurrentRow(boolean andEdit)
   {
      moveToIndex(currentIndex);
   }

   @Override
   public void gotoPrevNewRow()
   {
      moveBackwardAndFind(NEW_PREDICATE);
   }

   private void moveForwardAndFind(Predicate<ContentState> condition)
   {
      if (currentIndex == idIndexList.size() - 1)
      {
         // end of list
         return;
      }
      // int nextIndex = currentIndex + 1;
      // ListIterator<TransUnit> iterator = transUnits.listIterator(nextIndex);
      // while (iterator.hasNext())
      // {
      // TransUnit next = iterator.next();
      // if (condition.apply(next))
      // {
      // currentIndex = nextIndex;
      // break;
      // }
      // nextIndex++;
      // }
   }

   private void moveBackwardAndFind(Predicate<ContentState> condition)
   {
      // ListIterator<TransUnit> iterator =
      // transUnits.listIterator(currentIndex);
      // int prevIndex = currentIndex;
      // while(iterator.hasPrevious()) {
      // TransUnit transUnit = iterator.previous();
      // prevIndex--;
      // if (condition.apply(transUnit)) {
      // currentIndex = prevIndex;
      // break;
      // }
      // }
   }

   public boolean moveToIndex(int rowIndex)
   {
      if (rowIndex < 0 || rowIndex >= idIndexList.size())
      {
         return false;
      }
      boolean moved = rowIndex != currentIndex;
      currentIndex = rowIndex;
      return moved;
   }

   @Override
   public void setRowValueOverride(int row, TransUnit targetCell)
   {

   }

   public int getCurrentIndex()
   {
      return currentIndex;
   }
}
