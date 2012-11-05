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
package org.zanata.webtrans.shared.rpc;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class LoadOptionsResult implements DispatchResult
{
   private boolean showEditorButtons;

   private boolean enterKeySavesImmediately;

   private boolean filterByTranslated;

   private boolean filterByNeedReview;

   private boolean filterByUntraslated;

   private NavOption navOption;

   private int pageSize;

   private boolean showErrors;


   public boolean getShowEditorButtons()
   {
      return showEditorButtons;
   }

   public void setShowEditorButtons(boolean showEditorButtons)
   {
      this.showEditorButtons = showEditorButtons;
   }

   public boolean getEnterKeySavesImmediately()
   {
      return enterKeySavesImmediately;
   }

   public void setEnterKeySavesImmediately(boolean enterKeySavesImmediately)
   {
      this.enterKeySavesImmediately = enterKeySavesImmediately;
   }

   public boolean getFilterByTranslated()
   {
      return filterByTranslated;
   }

   public void setFilterByTranslated(boolean filterByTranslated)
   {
      this.filterByTranslated = filterByTranslated;
   }

   public boolean getFilterByNeedReview()
   {
      return filterByNeedReview;
   }

   public void setFilterByNeedReview(boolean filterByNeedReview)
   {
      this.filterByNeedReview = filterByNeedReview;
   }

   public boolean getFilterByUntraslated()
   {
      return filterByUntraslated;
   }

   public void setFilterByUntraslated(boolean filterByUntraslated)
   {
      this.filterByUntraslated = filterByUntraslated;
   }

   public NavOption getNavOption()
   {
      return navOption;
   }

   public void setNavOption(NavOption navOption)
   {
      this.navOption = navOption;
   }

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public boolean getShowErrors()
   {
      return showErrors;
   }

   public void setShowErrors(boolean showErrors)
   {
      this.showErrors = showErrors;
   }
}
