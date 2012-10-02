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
package org.zanata.service.impl;

import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.rest.service.ZPathService;

/**
 * Default implementation for the
 * {@link org.zanata.rest.service.StatisticsResource} interface. This is a
 * business/REST service.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path("/stats")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Name("statisticsServiceImpl")
@Scope(ScopeType.STATELESS)
@AutoCreate
@Transactional
@Slf4j
public class StatisticsServiceImpl implements StatisticsResource
{
   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private LocaleServiceImpl localeServiceImpl;

   @In
   private ZPathService zPathService;

   @Override
   public ContainerTranslationStatistics getStatistics(String projectSlug, String iterationSlug, boolean includeDetails, boolean includeWordStats, String[] locales)
   {
      LocaleId[] localeIds;

      // if no locales are specified, search in all locales
      if (locales.length == 0)
      {
         List<HLocale> iterationLocales = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, iterationSlug);
         localeIds = new LocaleId[iterationLocales.size()];
         for (int i = 0, iterationLocalesSize = iterationLocales.size(); i < iterationLocalesSize; i++)
         {
            HLocale loc = iterationLocales.get(i);
            localeIds[i] = loc.getLocaleId();
         }
      }
      else
      {
         localeIds = new LocaleId[locales.length];
         for (int i = 0; i < locales.length; i++)
         {
            localeIds[i] = new LocaleId(locales[i]);
         }
      }

      HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      if (iteration == null)
      {
         throw new NoSuchEntityException(projectSlug + "/" + iterationSlug);
      }

      Map<String, TransUnitWords> wordIterationStats = projectIterationDAO.getAllWordStatsStatistics(iteration.getId());
      Map<String, TransUnitCount> transUnitIterationStats = projectIterationDAO.getAllStatisticsForContainer(iteration.getId());

      ContainerTranslationStatistics iterationStats = new ContainerTranslationStatistics();
      iterationStats.setId(iterationSlug);
      iterationStats.addRef(new Link(URI.create(zPathService.generatePathForProjectIteration(iteration)), "statSource", "PROJ_ITER"));

      long iterationTotalMssgs = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      long iterationTotalWords = projectIterationDAO.getTotalWordCountForIteration(iteration.getId());

      for (LocaleId locId : localeIds)
      {
         // word level stats
         TransUnitWords wordCount = wordIterationStats.get(locId.getId());
         TranslationStatistics wordStats;

         if (wordCount == null)
         {
            wordCount = new TransUnitWords(0, 0, (int) iterationTotalWords);
         }
         wordStats = getWordsStats(wordCount, locId);
         wordStats.setRemainingHours(getRemainingHours(wordCount.get(ContentState.NeedReview), wordCount.get(ContentState.New)));
         iterationStats.addStats(wordStats);

         // trans unit level stats
         TransUnitCount count = transUnitIterationStats.get(locId.getId());
         TranslationStatistics transUnitStats;

         if (count == null)
         {
            count = new TransUnitCount(0, 0, (int) iterationTotalMssgs);
         }
         transUnitStats = getMessageStats(count, locId);
         transUnitStats.setRemainingHours(getRemainingHours(wordCount.get(ContentState.NeedReview), wordCount.get(ContentState.New)));
         iterationStats.addStats(transUnitStats);
      }

      // TODO Do in a single query
      if (includeDetails)
      {
         for (String docId : iteration.getDocuments().keySet())
         {
            iterationStats.addDetailedStats(this.getStatistics(projectSlug, iterationSlug, docId, includeWordStats, locales));
         }
      }

      return iterationStats;
   }

   @Override
   public ContainerTranslationStatistics getStatistics(String projectSlug, String iterationSlug, String docId, boolean includeWordStats, String[] locales)
   {
      LocaleId[] localeIds;

      // if no locales are specified, search in all locales
      if (locales.length == 0)
      {
         List<HLocale> iterationLocales = localeServiceImpl.getSupportedLangugeByProjectIteration(projectSlug, iterationSlug);
         localeIds = new LocaleId[iterationLocales.size()];
         for (int i = 0, iterationLocalesSize = iterationLocales.size(); i < iterationLocalesSize; i++)
         {
            HLocale loc = iterationLocales.get(i);
            localeIds[i] = loc.getLocaleId();
         }
      }
      else
      {
         localeIds = new LocaleId[locales.length];
         for (int i = 0; i < locales.length; i++)
         {
            localeIds[i] = new LocaleId(locales[i]);
         }
      }

      HDocument document = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);

      if (document == null)
      {
         throw new NoSuchEntityException(projectSlug + "/" + iterationSlug + "/" + docId);
      }

      Map<LocaleId, TranslationStats> statsMap = documentDAO.getStatistics(document.getId(), localeIds);

      ContainerTranslationStatistics docStats = new ContainerTranslationStatistics();
      docStats.setId(docId);
      docStats.addRef(new Link(URI.create(zPathService.generatePathForDocument(document)), "statSource", "DOC"));

      long docTotalMssgs = documentDAO.getTotalCountForDocument(document);
      long docTotalWords = documentDAO.getTotalWordCountForDocument(document);

      for (LocaleId locale : localeIds)
      {
         TranslationStats stats = statsMap.get(locale);
         TransUnitCount count;
         TransUnitWords wordCount;

         if (stats == null)
         {
            count = new TransUnitCount(0, 0, (int) docTotalMssgs);
            wordCount = new TransUnitWords(0, 0, (int) docTotalWords);
         }
         else
         {
            count = stats.getUnitCount();
            wordCount = stats.getWordCount();
         }

         // word level stats
         TranslationStatistics wordStats = getWordsStats(wordCount, locale);
         wordStats.setRemainingHours(getRemainingHours(wordCount.get(ContentState.NeedReview), wordCount.get(ContentState.New)));
         docStats.addStats(wordStats);

         // trans unit level stats
         TranslationStatistics transUnitStats = getMessageStats(count, locale);
         transUnitStats.setRemainingHours(getRemainingHours(wordCount.get(ContentState.NeedReview), wordCount.get(ContentState.New)));
         docStats.addStats(transUnitStats);
      }

      return docStats;
   }

   private TranslationStatistics getWordsStats(TransUnitWords wordCount, LocaleId locale)
   {
      TranslationStatistics stats = new TranslationStatistics();
      stats.setLocale(locale.getId());
      stats.setUnit(TranslationStatistics.StatUnit.WORD);
      stats.setTranslated(wordCount.get(ContentState.Approved));
      stats.setUntranslated(wordCount.get(ContentState.New));
      stats.setNeedReview(wordCount.get(ContentState.NeedReview));
      stats.setTotal(wordCount.getTotal());
      return stats;
   }

   private TranslationStatistics getMessageStats(TransUnitCount unitCount, LocaleId locale)
   {
      TranslationStatistics stats = new TranslationStatistics();
      stats.setLocale(locale.getId());
      stats.setUnit(TranslationStatistics.StatUnit.MESSAGE);
      stats.setTranslated(unitCount.get(ContentState.Approved));
      stats.setUntranslated(unitCount.get(ContentState.New));
      stats.setNeedReview(unitCount.get(ContentState.NeedReview));
      stats.setTotal(unitCount.getTotal());
      return stats;
   }

   private double getRemainingHours(double fuzzy, double untrans)
   {
      double untransHours = untrans / 250.0;
      double fuzzyHours = fuzzy / 500.0;
      double remainHours = untransHours + fuzzyHours;
      return remainHours;
   }
}
