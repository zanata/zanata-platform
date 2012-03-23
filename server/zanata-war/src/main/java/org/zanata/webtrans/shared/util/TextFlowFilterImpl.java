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
package org.zanata.webtrans.shared.util;

import org.zanata.common.ContentState;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.FilterConstraints;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class TextFlowFilterImpl implements TextFlowFilter
{
   private boolean filterTranslated, filterNeedReview, filterUntranslated;
   private String phrase;
   private boolean acceptAll;

   public TextFlowFilterImpl(FilterConstraints constraints)
   {
      this(constraints.getSearchString(), constraints.isIncludeApproved(), constraints.isIncludeFuzzy(), constraints.isIncludeNew());
   }

   public TextFlowFilterImpl(String phrase, boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated)
   {
      this.filterTranslated = filterTranslated;
      this.filterNeedReview = filterNeedReview;
      this.filterUntranslated = filterUntranslated;
      if (phrase != null && !phrase.isEmpty())
      {
         this.phrase = phrase.toLowerCase();
      }
      acceptAll = false;
   }

   public TextFlowFilterImpl()
   {
      acceptAll = true;
   }

   /**
    * Filter out according to target state
    * 
    * @param target
    * @return
    */
   @Override
   public boolean isFilterOut(HTextFlow textFlow, HLocale locale)
   {
      if (acceptAll)
      {
         return false;
      }

      HTextFlowTarget target = textFlow.getTargets().get(locale);
      if (isMatchSearch(textFlow, target))
      {
         if ((filterTranslated == filterNeedReview) && (filterNeedReview == filterUntranslated) && (filterUntranslated == filterTranslated))
         {
            return false;
         }

         if (target == null)
         {
            if (filterUntranslated)
            {
               return false;
            }
         }
         else
         {
            ContentState state = target.getState();
            if (state == ContentState.Approved && filterTranslated)
            {
               return false;
            }
            if (state == ContentState.NeedReview && filterNeedReview)
            {
               return false;
            }
            if (state == ContentState.New && filterUntranslated)
            {
               return false;
            }
         }
      }
      return true;
   }

   private boolean isMatchSearch(HTextFlow textFlow, HTextFlowTarget target)
   {
      if (phrase != null && !phrase.isEmpty())
      {
         if (target != null)
         {
            return textFlow.getContent().toLowerCase().contains(phrase) || target.getContent().toLowerCase().contains(phrase);
         }
         else
         {
            return textFlow.getContent().toLowerCase().contains(phrase);
         }
      }
      return true;
   }
}
