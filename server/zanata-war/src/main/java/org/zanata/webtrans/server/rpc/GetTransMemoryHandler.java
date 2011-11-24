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
package org.zanata.webtrans.server.rpc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.lucene.queryParser.ParseException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.LevenshteinUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.util.ShortString;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;

@Name("webtrans.gwt.GetTransMemoryHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTranslationMemory.class)
public class GetTransMemoryHandler extends AbstractActionHandler<GetTranslationMemory, GetTranslationMemoryResult>
{

   private static final int MAX_RESULTS = 10;

   @Logger
   private Log log;

   @In
   private LocaleService localeServiceImpl;

   @In
   private TextFlowDAO textFlowDAO;

   @Override
   public GetTranslationMemoryResult execute(GetTranslationMemory action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      final String searchText = action.getQuery();
      ShortString abbrev = new ShortString(searchText);
      final SearchType searchType = action.getSearchType();
      log.info("Fetching TM matches({0}) for \"{1}\"", searchType, abbrev);

      LocaleId localeID = action.getLocaleId();
      HLocale hLocale = localeServiceImpl.getByLocaleId(localeID);
      ArrayList<TranslationMemoryGlossaryItem> results;

      try
      {
         List<Long> translatedIds = textFlowDAO.getIdsByTargetState(localeID, ContentState.Approved);
         List<Object[]> matches = textFlowDAO.getSearchResult(searchText, searchType, translatedIds, MAX_RESULTS);
         Map<TMKey, TranslationMemoryGlossaryItem> matchesMap = new LinkedHashMap<TMKey, TranslationMemoryGlossaryItem>();
         for (Object[] match : matches)
         {
            float score = (Float) match[0];
            HTextFlow textFlow = (HTextFlow) match[1];
            if (textFlow == null || textFlow.getDocument().getProjectIteration().isObsolete() || textFlow.getDocument().getProjectIteration().getProject().isObsolete())
            {
               continue;
            }
            HTextFlowTarget target = textFlow.getTargets().get(hLocale);
            // double check in case of caching issues
            if (target.getState() != ContentState.Approved)
            {
               continue;
            }
            String textFlowContent = textFlow.getContent();
            String targetContent = target.getContent();

            int percent = (int) (100 * LevenshteinUtil.getSimilarity(searchText, textFlowContent));
            TMKey key = new TMKey(textFlowContent, targetContent);
            TranslationMemoryGlossaryItem item = matchesMap.get(key);
            if (item == null)
            {
               item = new TranslationMemoryGlossaryItem(textFlowContent, targetContent, score, percent);
               matchesMap.put(key, item);
            }
            item.addTransUnitId(textFlow.getId());
         }
         results = new ArrayList<TranslationMemoryGlossaryItem>(matchesMap.values());
      }
      catch (ParseException e)
      {
         if (searchType == SearchType.RAW)
         {
            log.warn("Can't parse raw query '" + searchText + "'");
         }
         else
         {
            // escaping failed!
            log.error("Can't parse query '" + searchText + "'", e);
         }
         results = new ArrayList<TranslationMemoryGlossaryItem>(0);
      }

      /**
       * NB just because this Comparator returns 0 doesn't mean the matches are
       * identical.
       */
      Comparator<TranslationMemoryGlossaryItem> comp = new Comparator<TranslationMemoryGlossaryItem>()
      {

         @Override
         public int compare(TranslationMemoryGlossaryItem m1, TranslationMemoryGlossaryItem m2)
         {
            int result;
            result = compare(m1.getSimilarityPercent(), m2.getSimilarityPercent());
            if (result != 0)
               return -result;
            result = compare(m1.getSource().length(), m2.getSource().length());
            if (result != 0)
               return result; // shorter matches are preferred, if similarity is
                              // the same
            result = compare(m1.getRelevanceScore(), m2.getRelevanceScore());
            if (result != 0)
               return -result;
            return m1.getSource().compareTo(m2.getSource());
         }

         private int compare(int a, int b)
         {
            if (a < b)
               return -1;
            if (a > b)
               return 1;
            return 0;
         }

         private int compare(float a, float b)
         {
            if (a < b)
               return -1;
            if (a > b)
               return 1;
            return 0;
         }

      };

      Collections.sort(results, comp);

      log.info("Returning {0} TM matches for \"{1}\"", results.size(), abbrev);
      return new GetTranslationMemoryResult(results);
   }

   @Override
   public void rollback(GetTranslationMemory action, GetTranslationMemoryResult result, ExecutionContext context) throws ActionException
   {
   }

   static class TMKey
   {

      private final String textFlowContent;
      private final String targetContent;

      public TMKey(String textFlowContent, String targetContent)
      {
         this.textFlowContent = textFlowContent;
         this.targetContent = targetContent;
      }

      public String getTextFlowContent()
      {
         return textFlowContent;
      }

      public String getTargetContent()
      {
         return targetContent;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof TMKey)
         {
            TMKey o = (TMKey) obj;
            return equal(textFlowContent, o.textFlowContent) && equal(targetContent, o.targetContent);
         }
         return false;
      }

      private static boolean equal(String s1, String s2)
      {
         return s1 == null ? s2 == null : s1.equals(s2);
      }

      @Override
      public int hashCode()
      {
         int result = 1;
         result = 37 * result + textFlowContent != null ? textFlowContent.hashCode() : 0;
         result = 37 * result + targetContent != null ? targetContent.hashCode() : 0;
         return result;
      }

   }

}
