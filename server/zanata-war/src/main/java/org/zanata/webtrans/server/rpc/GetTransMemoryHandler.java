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
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.LevenshteinUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

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

      log.info("Fetching matches for {0}", action.getQuery());

      LocaleId localeID = action.getLocaleId();
      HLocale hLocale = localeServiceImpl.getByLocaleId(localeID);
      ArrayList<TransMemoryResultItem> results;

      try
      {
         // FIXME this won't scale well
         List<Long> idsWithTranslations = textFlowDAO.findIdsWithTranslations(localeID);
         List<Object[]> matches = textFlowDAO.getSearchResult(action.getQuery(), idsWithTranslations, MAX_RESULTS);
         Map<TMKey, TransMemoryResultItem> matchesMap = new LinkedHashMap<TMKey, TransMemoryResultItem>(matches.size());
         for (Object[] match : matches)
         {
            float score = (Float) match[0];
            HTextFlow textFlow = (HTextFlow) match[1];
            if (textFlow == null)
            {
               continue;
            }
            else
            {
               HProjectIteration projectIteration = textFlow.getDocument().getProjectIteration();
               if (projectIteration.getStatus() == EntityStatus.OBSOLETE || projectIteration.getProject().getStatus() == EntityStatus.OBSOLETE)
               {
                  continue;
               }
            }
            HTextFlowTarget target = textFlow.getTargets().get(hLocale);
            // double check in case of caching issues
            if (target == null || target.getState() != ContentState.Approved)
            {
               continue;
            }

            double percent;
            if (action.getQuery().getSearchType() == SearchType.FUZZY_PLURAL)
            {
               percent = 100 * LevenshteinUtil.getSimilarity(action.getQuery().getQueries(), textFlow.getContents());
            }
            else
            {
               final String searchText = action.getQuery().getQueries().get(0);
               percent = 100 * LevenshteinUtil.getSimilarity(searchText, textFlow.getContents());
            }
            ArrayList<String> textFlowContents = new ArrayList<String>(textFlow.getContents());
            ArrayList<String> targetContents = new ArrayList<String>(target.getContents());
            TMKey key = new TMKey(textFlowContents, targetContents);
            TransMemoryResultItem item = matchesMap.get(key);
            if (item == null)
            {
               item = new TransMemoryResultItem(textFlowContents, targetContents, score, percent);
               matchesMap.put(key, item);
            }
            item.addSourceId(textFlow.getId());
         }
         results = new ArrayList<TransMemoryResultItem>(matchesMap.values());
      }
      catch (ParseException e)
      {
         if (action.getQuery().getSearchType() == SearchType.RAW)
         {
            // TODO tell the user
            log.warn("Can't parse raw query " + action.getQuery());
         }
         else
         {
            // escaping failed!
            log.error("Can't parse query " + action.getQuery(), e);
         }
         results = new ArrayList<TransMemoryResultItem>(0);
      }

      /**
       * NB just because this Comparator returns 0 doesn't mean the matches are
       * identical.
       */
      Comparator<TransMemoryResultItem> comp = new Comparator<TransMemoryResultItem>()
      {

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
            if (list1.size() < list2.size())
            {
               return -1;
            }
            else if (list1.size() > list2.size())
            {
               return 1;
            }
            return 0;
         }

      };

      Collections.sort(results, comp);

      log.info("Returning {0} TM matches for {1}", results.size(), action.getQuery());
      return new GetTranslationMemoryResult(action, results);
   }

   @Override
   public void rollback(GetTranslationMemory action, GetTranslationMemoryResult result, ExecutionContext context) throws ActionException
   {
   }

   static class TMKey
   {

      private final List<String> textFlowContents;
      private final List<String> targetContents;

      public TMKey(List<String> textFlowContents, List<String> targetContents)
      {
         this.textFlowContents = textFlowContents;
         this.targetContents = targetContents;
      }

      public List<String> getTextFlowContents()
      {
         return textFlowContents;
      }

      public List<String> getTargetContents()
      {
         return targetContents;
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
         int result = 1;
         result = 37 * result + (textFlowContents != null ? textFlowContents.hashCode() : 0);
         result = 37 * result + (targetContents != null ? targetContents.hashCode() : 0);
         return result;
      }

   }

}
