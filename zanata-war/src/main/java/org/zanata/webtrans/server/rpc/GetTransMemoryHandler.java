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

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.lucene.queryParser.ParseException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.LevenshteinTokenUtil;
import org.zanata.search.LevenshteinUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

@Name("webtrans.gwt.GetTransMemoryHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetTranslationMemory.class)
@Slf4j
public class GetTransMemoryHandler extends AbstractActionHandler<GetTranslationMemory, GetTranslationMemoryResult>
{

   private static final int MAX_RESULTS = 10;

   @In
   private LocaleService localeServiceImpl;

   @In
   private TextFlowDAO textFlowDAO;

   @Override
   public GetTranslationMemoryResult execute(GetTranslationMemory action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      TransMemoryQuery transMemoryQuery = action.getQuery();
      log.debug("Fetching matches for {}", transMemoryQuery);

      HLocale hLocale = localeServiceImpl.getByLocaleId(action.getLocaleId().getId());
      HLocale sourceLocale = localeServiceImpl.getByLocaleId(action.getSourceLocaleId().getId());

      ArrayList<TransMemoryResultItem> results = searchTransMemory(hLocale, sourceLocale, transMemoryQuery);

      log.debug("Returning {} TM matches for {}", results.size(), transMemoryQuery);
      return new GetTranslationMemoryResult(action, results);
   }

   protected ArrayList<TransMemoryResultItem> searchTransMemory(HLocale hLocale, HLocale sourceLocale, TransMemoryQuery transMemoryQuery)
   {
      ArrayList<TransMemoryResultItem> results = Lists.newArrayList();
      try
      {
         List<Object[]> matches = textFlowDAO.getSearchResult(transMemoryQuery, sourceLocale.getLocaleId(), MAX_RESULTS);

         Map<TMKey, TransMemoryResultItem> matchesMap = new LinkedHashMap<TMKey, TransMemoryResultItem>(matches.size());
         for (Object[] match : matches)
         {
            float score = (Float) match[0];
            HTextFlowTarget textFlowTarget = (HTextFlowTarget) match[1];

            if (isInvalidResult(textFlowTarget, hLocale))
            {
               continue;
            }

            double percent = calculateSimilarityPercentage(transMemoryQuery, textFlowTarget.getTextFlow().getContents());

            ArrayList<String> textFlowContents = new ArrayList<String>(textFlowTarget.getTextFlow().getContents());
            ArrayList<String> targetContents = new ArrayList<String>(textFlowTarget.getTextFlow().getTargets().get(hLocale.getId()).getContents());

            TMKey key = new TMKey(textFlowContents, targetContents);
            TransMemoryResultItem item = matchesMap.get(key);
            if (item == null)
            {
               item = new TransMemoryResultItem(textFlowContents, targetContents, score, percent);
               matchesMap.put(key, item);
            }
            item.incMatchCount();
            item.addSourceId(textFlowTarget.getTextFlow().getId());
         }
         results.addAll(matchesMap.values());
      }
      catch (ParseException e)
      {
         if (transMemoryQuery.getSearchType() == SearchType.RAW)
         {
            // TODO tell the user
            log.warn("Can't parse raw query {}", transMemoryQuery);
         }
         else
         {
            // escaping failed!
            log.error("Can't parse query " + transMemoryQuery, e);
         }
      }

      Collections.sort(results, TransMemoryResultComparator.COMPARATOR);
      return results;
   }

   private static boolean isInvalidResult(HTextFlowTarget textFlowTarget, HLocale hLocale)
   {
      if (textFlowTarget == null)
      {
         return true;
      }
      else
      {
         HProjectIteration projectIteration = textFlowTarget.getTextFlow().getDocument().getProjectIteration();
         if (projectIteration.getStatus() == EntityStatus.OBSOLETE || projectIteration.getProject().getStatus() == EntityStatus.OBSOLETE)
         {
            return true;
         }
      }
      HTextFlowTarget target = textFlowTarget.getTextFlow().getTargets().get(hLocale.getId());
      // double check in case of caching issues
      return target == null || target.getState() != ContentState.Approved;
   }

   private static double calculateSimilarityPercentage(TransMemoryQuery query, List<String> sourceContents)
   {
      double percent;
      // TODO use LevenshteinTokenUtil, *but* we need to distinguish 100% token match from 100% text match
      if (query.getSearchType() == SearchType.FUZZY_PLURAL)
      {
         percent = 100 * LevenshteinTokenUtil.getSimilarity(query.getQueries(), sourceContents);
         if (percent > 99.99)
         {
            // make sure we only get 100% similarity if every character matches
            percent = 100 * LevenshteinUtil.getSimilarity(query.getQueries(), sourceContents);
         }
      }
      else
      {
         final String searchText = query.getQueries().get(0);
         percent = 100 * LevenshteinTokenUtil.getSimilarity(searchText, sourceContents);
         if (percent > 99.99)
         {
            // make sure we only get 100% similarity if every character matches
            percent = 100 * LevenshteinUtil.getSimilarity(searchText, sourceContents);
         }
      }
      return percent;
   }

   @Override
   public void rollback(GetTranslationMemory action, GetTranslationMemoryResult result, ExecutionContext context) throws ActionException
   {
   }

   private static class TMKey
   {
      private final List<String> textFlowContents;
      private final List<String> targetContents;

      private TMKey(List<String> textFlowContents, List<String> targetContents)
      {
         this.textFlowContents = textFlowContents;
         this.targetContents = targetContents;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof TMKey)
         {
            TMKey o = (TMKey) obj;
            return textFlowContents.equals(o.textFlowContents) && targetContents.equals(o.targetContents);
         }
         return false;
      }

      @Override
      public int hashCode()
      {
         return Objects.hashCode(textFlowContents, targetContents);
      }
   }

   /**
    * NB just because this Comparator returns 0 doesn't mean the matches are
    * identical.
    */
   private static enum TransMemoryResultComparator implements Comparator<TransMemoryResultItem>
   {
      COMPARATOR;

      @Override
      public int compare(TransMemoryResultItem m1, TransMemoryResultItem m2)
      {
         int result;
         result = Double.compare(m1.getSimilarityPercent(), m2.getSimilarityPercent());
         if (result != 0)
         {
            // sort higher similarity first
            return -result;
         }
         result = compare(m1.getSourceContents(), m2.getSourceContents());
         // sort longer string lists first (more plural forms)
         return -result;
      }

      private int compare(List<String> list1, List<String> list2)
      {
         for (int i = 0; i < list1.size() && i < list2.size(); i++)
         {
            String s1 = list1.get(i);
            String s2 = list2.get(i);
            int comp = s1.compareTo(s2);
            if (comp != 0)
            {
               return comp;
            }
         }
         return list1.size() - list2.size();
      }

   }
}
