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
import org.zanata.webtrans.shared.rpc.NavOption;
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
   
   // default state
   private ConfigurationState state = new ConfigurationState(false, false, true, 10, NavOption.FUZZY_UNTRANSLATED);

   public boolean isEnterSavesApproved()
   {
      return state.isEnterSavesApproved();
   }

   protected void setEnterSavesApproved(boolean enterSavesApproved)
   {
      state = new ConfigurationState(enterSavesApproved, state.isEscClosesEditor(), state.isDisplayButtons(), state.getPageSize(), state.getNavOption());
   }

   public boolean isEscClosesEditor()
   {
      return state.isEscClosesEditor();
   }

   protected void setEscClosesEditor(boolean escClosesEditor)
   {
      state = new ConfigurationState(state.isEnterSavesApproved(), escClosesEditor, state.isDisplayButtons(), state.getPageSize(), state.getNavOption());
   }

   public boolean isDisplayButtons()
   {
      return state.isDisplayButtons();
   }

   protected void setDisplayButtons(boolean displayButtons)
   {
      state = new ConfigurationState(state.isEnterSavesApproved(), state.isEscClosesEditor(), displayButtons, state.getPageSize(), state.getNavOption());
   }

   protected void setNavOption(NavOption navOption)
   {
      state = new ConfigurationState(state.isEnterSavesApproved(), state.isEscClosesEditor(), state.isDisplayButtons(), state.getPageSize(), navOption);
   }

   public NavOption getNavOption()
   {
      return state.getNavOption();
   }

   public Predicate<ContentState> getContentStatePredicate()
   {
      if (state.getNavOption() == NavOption.FUZZY_UNTRANSLATED)
      {
         return FUZZY_OR_NEW_PREDICATE;
      }
      else if (state.getNavOption() == NavOption.FUZZY)
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
      return state.getPageSize();
   }

   protected void setPageSize(int pageSize)
   {
      state = new ConfigurationState(state.isEnterSavesApproved(), state.isEscClosesEditor(), state.isDisplayButtons(), pageSize, state.getNavOption());
   }

   public ConfigurationState getState()
   {
      return state;
   }

   /**
    * Immutable object represents configuration state
    */
   public static class ConfigurationState
   {
      private boolean enterSavesApproved;
      private boolean escClosesEditor;
      private boolean displayButtons;
      private int pageSize;
      private NavOption navOption;

      private ConfigurationState(boolean enterSavesApproved, boolean escClosesEditor, boolean displayButtons, int pageSize, NavOption navOption)
      {
         this.enterSavesApproved = enterSavesApproved;
         this.escClosesEditor = escClosesEditor;
         this.displayButtons = displayButtons;
         this.pageSize = pageSize;
         this.navOption = navOption;
      }

      public boolean isEnterSavesApproved()
      {
         return enterSavesApproved;
      }

      public boolean isEscClosesEditor()
      {
         return escClosesEditor;
      }

      public boolean isDisplayButtons()
      {
         return displayButtons;
      }

      public int getPageSize()
      {
         return pageSize;
      }

      public NavOption getNavOption()
      {
         return navOption;
      }
   }
}
