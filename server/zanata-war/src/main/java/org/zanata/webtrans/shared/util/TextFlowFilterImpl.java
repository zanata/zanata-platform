/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
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
@Name("textFlowFilterImpl")
@AutoCreate
@Scope(ScopeType.EVENT)
public class TextFlowFilterImpl implements TextFlowFilter
{
   private boolean includeTranslated, includeNeedReview, includeUntranslated;
   private String phrase;

   public TextFlowFilterImpl(FilterConstraints constraints)
   {
      this(constraints.getSearchString(), constraints.isIncludeApproved(), constraints.isIncludeFuzzy(), constraints.isIncludeNew());
   }

   //TODO phase this constructor out?
   public TextFlowFilterImpl(String phrase, boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated)
   {
      this.includeTranslated = filterTranslated;
      this.includeNeedReview = filterNeedReview;
      this.includeUntranslated = filterUntranslated;
      if (phrase == null)
      {
         this.phrase = "";
      }
      else
      {
         this.phrase = phrase.toLowerCase();
      }
   }

   public TextFlowFilterImpl()
   {
      includeTranslated = true;
      includeNeedReview = true;
      includeUntranslated = true;
      phrase = "";
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
      if (isAcceptAll())
      {
         return false;
      }

      
      HTextFlowTarget target = textFlow.getTargets().get(locale);
      if (isMatchSearch(textFlow, target))
      {
         if ((includeTranslated == includeNeedReview) && (includeNeedReview == includeUntranslated) && (includeUntranslated == includeTranslated))
         {
            return false;
         }

         if (target == null)
         {
            if (includeUntranslated)
            {
               return false;
            }
         }
         else
         {
            ContentState state = target.getState();
            if (state == ContentState.Approved && includeTranslated)
            {
               return false;
            }
            if (state == ContentState.NeedReview && includeNeedReview)
            {
               return false;
            }
            if (state == ContentState.New && includeUntranslated)
            {
               return false;
            }
         }
      }
      return true;
   }

   private boolean isMatchSearch(HTextFlow textFlow, HTextFlowTarget target)
   {
      if (phrase == null || phrase.isEmpty())
      {
         return true;
      }

      //TODO check filter constraints for whether to check source and target
      boolean phraseInSource = false;
      for (String source : textFlow.getContents())
      {
         if (source.toLowerCase().contains(phrase))
         {
            phraseInSource = true;
            break;
         }
      }

      boolean phraseInTarget = false;
      if (target != null)
      {
         for (String targ : target.getContents())
         {
            if (targ.toLowerCase().contains(phrase))
            {
               phraseInTarget = true;
               break;
            }
         }
      }

      return phraseInSource || phraseInTarget;
   }

   public boolean isAcceptAll()
   {
      return phrase.isEmpty() && includeUntranslated && includeNeedReview && includeTranslated;
   }
}
