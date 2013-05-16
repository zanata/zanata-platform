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
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.rest.service.ZPathService;
import org.zanata.service.TranslationStateCache;
import org.zanata.util.DateUtil;
import org.zanata.webtrans.shared.model.DocumentStatus;

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

   @In
   private TranslationStateCache translationStateCacheImpl;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

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

      // FIXME rhbz953734 this needs to fix
      Map<String, TransUnitCount> transUnitIterationStats = projectIterationDAO.getAllStatisticsForContainer(iteration.getId());
      Map<String, TransUnitWords> wordIterationStats = null;
      if (includeWordStats)
      {
         wordIterationStats = projectIterationDAO.getAllWordStatsStatistics(iteration.getId());
      }
      ContainerTranslationStatistics iterationStats = new ContainerTranslationStatistics();
      iterationStats.setId(iterationSlug);
      iterationStats.addRef(new Link(URI.create(zPathService.generatePathForProjectIteration(iteration)), "statSource", "PROJ_ITER"));
      long iterationTotalMssgs = projectIterationDAO.getTotalCountForIteration(iteration.getId());
      long iterationTotalWords = projectIterationDAO.getTotalWordCountForIteration(iteration.getId());

      for (LocaleId locId : localeIds)
      {
         // trans unit level stats
         TransUnitCount count = transUnitIterationStats.get(locId.getId());
         // Stats might not return anything if nothing is translated
         if (count == null)
         {
            count = new TransUnitCount(0, 0, (int) iterationTotalMssgs, 0, 0);
         }

         HTextFlowTarget target = localeServiceImpl.getLastTranslated(projectSlug, iterationSlug, locId);

         String lastModifiedBy = "";
         Date lastModifiedDate = null;

         if (target != null)
         {
            lastModifiedDate = target.getLastChanged();
            if (target.getLastModifiedBy() != null)
            {
               lastModifiedBy = target.getLastModifiedBy().getAccount().getUsername();
            }
         }

         TranslationStatistics transUnitStats = getMessageStats(count, locId, lastModifiedDate, lastModifiedBy);
         transUnitStats.setRemainingHours(getRemainingHours(count.get(ContentState.NeedReview), count.get(ContentState.New)));
         iterationStats.addStats(transUnitStats);

         // word level stats
         if (includeWordStats)
         {
            TransUnitWords wordCount = wordIterationStats.get(locId.getId());
            if (wordCount == null)
            {
               wordCount = new TransUnitWords(0, 0, (int) iterationTotalWords, 0, 0);
            }

            TranslationStatistics wordsStats = getWordsStats(wordCount, locId, lastModifiedDate, lastModifiedBy);
            wordsStats.setRemainingHours(getRemainingHours(wordCount.get(ContentState.NeedReview), wordCount.get(ContentState.New)));
            iterationStats.addStats(wordsStats);
         }
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

      long docTotalWords = 0;
      if (includeWordStats)
      {
         docTotalWords = documentDAO.getTotalWordCountForDocument(document);
      }

      for (LocaleId locale : localeIds)
      {
         TranslationStats stats = statsMap.get(locale);

         // trans unit level stats
         TransUnitCount count;
         if (stats == null)
         {
            count = new TransUnitCount(0, 0, (int) docTotalMssgs, 0, 0);
         }
         else
         {
            count = stats.getUnitCount();
         }
         DocumentStatus docStat = translationStateCacheImpl.getDocStats(document.getId(), locale);

         TranslationStatistics transUnitStats = getMessageStats(count, locale, docStat.getLastTranslatedDate(), docStat.getLastTranslatedBy());
         transUnitStats.setRemainingHours(getRemainingHours(count.get(ContentState.NeedReview), count.get(ContentState.New)));
         docStats.addStats(transUnitStats);

         // word level stats
         if (includeWordStats)
         {
            TransUnitWords wordCount;
            if (stats == null)
            {
               wordCount = new TransUnitWords(0, 0, (int) docTotalWords, 0, 0);
            }
            else
            {
               wordCount = stats.getWordCount();
            }
            TranslationStatistics wordsStats = getWordsStats(wordCount, locale, docStat.getLastTranslatedDate(), docStat.getLastTranslatedBy());
            wordsStats.setRemainingHours(getRemainingHours(wordCount.get(ContentState.NeedReview), wordCount.get(ContentState.New)));
            docStats.addStats(wordsStats);
         }
      }

      return docStats;
   }

   private TranslationStatistics getWordsStats(TransUnitWords wordCount, LocaleId locale, Date lastChanged, String lastModifiedBy)
   {
      TranslationStatistics stats = new TranslationStatistics();
      stats.setLocale(locale.getId());
      stats.setUnit(TranslationStatistics.StatUnit.WORD);
      stats.setTranslated(wordCount.get(ContentState.Approved));
      stats.setUntranslated(wordCount.get(ContentState.New));
      stats.setNeedReview(wordCount.get(ContentState.NeedReview));
      stats.setTotal(wordCount.getTotal());
      stats.setLastTranslated(getLastTranslated(lastChanged, lastModifiedBy));
      return stats;
   }

   private TranslationStatistics getMessageStats(TransUnitCount unitCount, LocaleId locale, Date lastChanged, String lastModifiedBy)
   {
      TranslationStatistics stats = new TranslationStatistics();
      stats.setLocale(locale.getId());
      stats.setUnit(TranslationStatistics.StatUnit.MESSAGE);
      stats.setTranslated(unitCount.get(ContentState.Approved));
      stats.setUntranslated(unitCount.get(ContentState.New));
      stats.setNeedReview(unitCount.get(ContentState.NeedReview));
      stats.setTotal(unitCount.getTotal());

      stats.setLastTranslated(getLastTranslated(lastChanged, lastModifiedBy));
      return stats;
   }

   private String getLastTranslated(Date lastChanged, String lastModifiedBy)
   {
      StringBuilder result = new StringBuilder();

      if (lastChanged != null)
      {
         result.append(DateUtil.formatShortDate(lastChanged));

         if (!StringUtils.isEmpty(lastModifiedBy))
         {
            result.append(" by ");
            result.append(lastModifiedBy);
         }
      }
      return result.toString();
   }

   private double getRemainingHours(double fuzzy, double untrans)
   {
      double untransHours = untrans / 250.0;
      double fuzzyHours = fuzzy / 500.0;
      double remainHours = untransHours + fuzzyHours;
      return remainHours;
   }

   public TranslationStats getDocStatistics(Long documentId, LocaleId localeId)
   {
      return documentDAO.getStatistics(documentId, localeId);
   }
}
