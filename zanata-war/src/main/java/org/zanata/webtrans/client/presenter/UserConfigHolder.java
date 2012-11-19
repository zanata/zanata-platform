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
import com.google.gwt.user.client.rpc.IsSerializable;
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
   
   private ConfigurationState state;

   public UserConfigHolder()
   {
      // default state
      state = new ConfigurationState();
      state.displayButtons = true;
      state.enterSavesApproved = false;
      state.editorPageSize = 25;
      state.documentListPageSize = 25;
      state.navOption = NavOption.FUZZY_UNTRANSLATED;
      state.showError =false;
      state.useCodeMirrorEditor = false;
   }

   public boolean isEnterSavesApproved()
   {
      return state.isEnterSavesApproved();
   }

   public void setEnterSavesApproved(boolean enterSavesApproved)
   {
      state = new ConfigurationState(state);
      state.enterSavesApproved = enterSavesApproved;
   }

   public boolean isDisplayButtons()
   {
      return state.isDisplayButtons();
   }

   public void setDisplayButtons(boolean displayButtons)
   {
      state = new ConfigurationState(state);
      state.displayButtons = displayButtons;
   }

   public void setNavOption(NavOption navOption)
   {
      state = new ConfigurationState(state);
      state.navOption = navOption;
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

   public int getEditorPageSize()
   {
      return state.getEditorPageSize();
   }

   public void setEditorPageSize(int editorPageSize)
   {
      state = new ConfigurationState(state);
      state.editorPageSize = editorPageSize;
   }

   public int getDocumentListPageSize()
   {
      return state.getDocumentListPageSize();
   }

   public void setDocumentListPageSize(int documentListPageSize)
   {
      state = new ConfigurationState(state);
      state.documentListPageSize = documentListPageSize;
   }

   public ConfigurationState getState()
   {
      return state;
   }

   /**
    * Sets all properties of the given state into this holder.
    *
    * @param state configuration state holder
    */
   public void setState( ConfigurationState state )
   {
      this.state = new ConfigurationState(state);
   }

   public boolean isShowError()
   {
      return state.isShowError();
   }

   public void setShowError(boolean showError)
   {
      state = new ConfigurationState(state);
      state.showError = showError;
   }

   public boolean isUseCodeMirrorEditor()
   {
      return state.isUseCodeMirrorEditor();
   }

   public void setUseCodeMirrorEditor(boolean useCodeMirrorEditor)
   {
      state = new ConfigurationState(state);
      state.useCodeMirrorEditor = useCodeMirrorEditor;
   }

   public void setFilterByUntranslated(boolean filterByUntranslated)
   {
      state = new ConfigurationState(state);
      state.filterByUntranslated = filterByUntranslated;
   }

   public void setFilterByNeedReview(boolean filterByNeedReview)
   {
      state = new ConfigurationState(state);
      state.filterByNeedReview = filterByNeedReview;
   }

   public void setFilterByTranslated(boolean filterByTranslated)
   {
      state = new ConfigurationState(state);
      state.filterByTranslated = filterByTranslated;
   }

   /**
    * Immutable object represents configuration state
    */
   public static class ConfigurationState implements IsSerializable
   {
      private boolean enterSavesApproved;
      private boolean displayButtons;
      private int editorPageSize;
      private int documentListPageSize;
      private NavOption navOption;
      private boolean showError;
      private boolean useCodeMirrorEditor;

      private boolean filterByUntranslated;
      private boolean filterByNeedReview;
      private boolean filterByTranslated;

      // Needed for GWT serialization
      private ConfigurationState()
      {
      }

      private ConfigurationState(ConfigurationState old)
      {
         this.enterSavesApproved = old.isEnterSavesApproved();
         this.displayButtons = old.isDisplayButtons();
         this.editorPageSize = old.getEditorPageSize();
         this.documentListPageSize = old.getDocumentListPageSize();
         this.navOption = old.getNavOption();
         this.showError = old.isShowError();
         this.useCodeMirrorEditor = old.isUseCodeMirrorEditor();
         this.filterByUntranslated = old.isFilterByUntranslated();
         this.filterByNeedReview = old.isFilterByNeedReview();
         this.filterByTranslated = old.isFilterByTranslated();
      }

      public boolean isEnterSavesApproved()
      {
         return enterSavesApproved;
      }

      public boolean isDisplayButtons()
      {
         return displayButtons;
      }

      public int getEditorPageSize()
      {
         return editorPageSize;
      }

      public int getDocumentListPageSize()
      {
         return documentListPageSize;
      }

      public NavOption getNavOption()
      {
         return navOption;
      }

      public boolean isShowError()
      {
         return showError;
      }

      public boolean isUseCodeMirrorEditor()
      {
         return useCodeMirrorEditor;
      }

      public boolean isFilterByUntranslated()
      {
         return filterByUntranslated;
      }

      public boolean isFilterByNeedReview()
      {
         return filterByNeedReview;
      }

      public boolean isFilterByTranslated()
      {
         return filterByTranslated;
      }
   }
}
