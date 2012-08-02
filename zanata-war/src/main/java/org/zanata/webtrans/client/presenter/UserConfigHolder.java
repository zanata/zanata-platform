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
package org.zanata.webtrans.client.presenter;

import org.zanata.common.ContentState;
import com.google.common.base.Predicate;
import com.google.inject.Singleton;


@Singleton
public class UserConfigHolder
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
   private boolean enterSavesApproved = false;
   private boolean escClosesEditor = false;
   private boolean buttonFuzzy = true;
   private boolean buttonUntranslated = true;
   private boolean displayButtons = true;
   private int pageSize = 5;

   public boolean isEnterSavesApproved()
   {
      return enterSavesApproved;
   }

   void setEnterSavesApproved(boolean enterSavesApproved)
   {
      this.enterSavesApproved = enterSavesApproved;
   }

   public boolean isEscClosesEditor()
   {
      return escClosesEditor;
   }

   void setEscClosesEditor(boolean escClosesEditor)
   {
      this.escClosesEditor = escClosesEditor;
   }

   public boolean isButtonFuzzy()
   {
      return buttonFuzzy;
   }

   void setButtonFuzzy(boolean buttonFuzzy)
   {
      this.buttonFuzzy = buttonFuzzy;
   }

   public boolean isButtonUntranslated()
   {
      return buttonUntranslated;
   }

   void setButtonUntranslated(boolean buttonUntranslated)
   {
      this.buttonUntranslated = buttonUntranslated;
   }

   public boolean isDisplayButtons()
   {
      return displayButtons;
   }

   //TODO TableEditorPresenter will call this one workspaceContext change event. Thus the method must be public.
   public void setDisplayButtons(boolean displayButtons)
   {
      this.displayButtons = displayButtons;
   }

   public boolean isFuzzyAndUntranslated()
   {
      return buttonFuzzy && buttonUntranslated;
   }

   public Predicate<ContentState> getContentStatePredicate()
   {
      if (isFuzzyAndUntranslated())
      {
         return FUZZY_OR_NEW_PREDICATE;
      }
      else if (isButtonFuzzy())
      {
         return FUZZY_PREDICATE;
      }
      else
      {
         return NEW_PREDICATE;
      }
   }

   public int getPageSize()
   {
      return pageSize;
   }

   void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }
}
