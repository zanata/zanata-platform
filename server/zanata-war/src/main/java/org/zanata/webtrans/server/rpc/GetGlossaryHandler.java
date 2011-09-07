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
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.search.LevenshteinUtil;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.util.ShortString;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossary.SearchType;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;

@Name("webtrans.gwt.GetGlossaryHandler")
@Scope(ScopeType.STATELESS)
@ActionHandlerFor(GetGlossary.class)
public class GetGlossaryHandler extends AbstractActionHandler<GetGlossary, GetGlossaryResult>
{

   private static final int MAX_RESULTS = 20;

   @Logger
   private Log log;

   @In
   private LocaleService localeServiceImpl;

   @In
   private GlossaryDAO glossaryDAO;

   /**
    * Filtered term ids First filter: Entries that contains target locale Second
    * filter: Source term in filtered entries
    * 
    * @param entries
    */
   private List<Long> getFilteredSrcTermIds(List<HGlossaryEntry> entries)
   {
      List<Long> termIds = new ArrayList<Long>();
      for (HGlossaryEntry entry : entries)
      {
         HGlossaryTerm term = entry.getGlossaryTerms().get(entry.getSrcLocale());
         if (term != null)
         {
            termIds.add(term.getId());
         }
      }
      return termIds;
   }

   private HGlossaryTerm getTargetTerm(List<HGlossaryEntry> entries, Long id, HLocale locale)
   {
      HGlossaryTerm targetTerm = null;
      for (HGlossaryEntry entry : entries)
      {
         if (entry.getId() == id)
         {
            return entry.getGlossaryTerms().get(locale);
         }
      }
      return targetTerm;

   }

   @Override
   public GetGlossaryResult execute(GetGlossary action, ExecutionContext context) throws ActionException
   {
      ZanataIdentity.instance().checkLoggedIn();

      final String searchText = action.getQuery();
      ShortString abbrev = new ShortString(searchText);
      final SearchType searchType = action.getSearchType();
      log.info("Fetching Glossary matches({0}) for \"{1}\"", searchType, abbrev);

      LocaleId localeID = action.getLocaleId();
      HLocale hLocale = localeServiceImpl.getByLocaleId(localeID);
      ArrayList<TranslationMemoryGlossaryItem> results;

      try
      {
         List<HGlossaryEntry> entries = glossaryDAO.getEntriesByLocaleId(localeID);
         List<Long> termIds = getFilteredSrcTermIds(entries);

         List<Object[]> matches = glossaryDAO.getSearchResult(searchText, searchType, termIds, MAX_RESULTS);

         Map<GlossaryKey, TranslationMemoryGlossaryItem> matchesMap = new LinkedHashMap<GlossaryKey, TranslationMemoryGlossaryItem>();
         for (Object[] match : matches)
         {
            float score = (Float) match[0];
            HGlossaryTerm glossaryTerm = (HGlossaryTerm) match[1];
            if (glossaryTerm == null)
            {
               continue;
            }

            String srcTermContent = glossaryTerm.getContent();
            HGlossaryTerm targetTerm = getTargetTerm(entries, glossaryTerm.getGlossaryEntry().getId(), hLocale);
            String targetTermContent = targetTerm.getContent();

            int levDistance = LevenshteinUtil.getLevenshteinSubstringDistance(searchText, srcTermContent);
            int maxDistance = searchText.length();
            int percent = 100 * (maxDistance - levDistance) / maxDistance;

            GlossaryKey key = new GlossaryKey(targetTermContent, srcTermContent);
            TranslationMemoryGlossaryItem item = matchesMap.get(key);
            if (item == null)
            {
               item = new TranslationMemoryGlossaryItem(srcTermContent, targetTermContent, score, percent);
               matchesMap.put(key, item);
            }
            item.addTransUnitId(glossaryTerm.getId());
         }
         results = new ArrayList<TranslationMemoryGlossaryItem>(matchesMap.values());
      }
      catch (ParseException e)
      {
         if (searchType == SearchType.FUZZY)
         {
            log.warn("Can't parse fuzzy query '" + searchText + "'");
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

      log.info("Returning {0} Glossary matches for \"{1}\"", results.size(), abbrev);
      return new GetGlossaryResult(results);
   }

   @Override
   public void rollback(GetGlossary action, GetGlossaryResult result, ExecutionContext context) throws ActionException
   {
   }

   static class GlossaryKey
   {

      private final String srcTermContent;
      private final String targetTermContent;

      public GlossaryKey(String srcTermContent, String targetTermContent)
      {
         this.srcTermContent = srcTermContent;
         this.targetTermContent = targetTermContent;
      }

      public String getSrcTermContent()
      {
         return srcTermContent;
      }

      public String getTargetTermContent()
      {
         return targetTermContent;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj instanceof GlossaryKey)
         {
            GlossaryKey o = (GlossaryKey) obj;
            return equal(srcTermContent, o.srcTermContent) && equal(targetTermContent, o.targetTermContent);
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
         result = 37 * result + srcTermContent != null ? srcTermContent.hashCode() : 0;
         result = 37 * result + targetTermContent != null ? targetTermContent.hashCode() : 0;
         return result;
      }

   }

}
