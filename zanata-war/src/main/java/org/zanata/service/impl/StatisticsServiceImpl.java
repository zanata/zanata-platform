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
import java.util.Arrays;
import java.util.Map;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
import org.zanata.model.HProjectIteration;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.rest.service.ZPathService;

import lombok.extern.slf4j.Slf4j;

/**
 * Default implementation for the {@link org.zanata.rest.service.StatisticsResource} interface.
 * This is a business/REST service.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Path("/stats")
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
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
   private ZPathService zPathService;


   @Override
   public ContainerTranslationStatistics
   getStatistics(String projectSlug, String iterationSlug, boolean includeDetails, boolean includeWordStats, String[] locales)
   {
      LocaleId[] localeIds = new LocaleId[ locales.length ];
      for( int i=0; i<locales.length; i++ )
      {
         localeIds[i] = new LocaleId( locales[i] );
      }
      Arrays.sort( locales ); // sort the array for binary search

      HProjectIteration iteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      if( iteration == null )
      {
         throw new NoSuchEntityException(projectSlug + "/" + iterationSlug);
      }

      Map<String, TransUnitCount> transUnitIterationStats = projectIterationDAO.getAllStatisticsForContainer(iteration.getId());
      Map<String, TransUnitWords> wordIterationStats = null;

      if( includeWordStats )
      {
         wordIterationStats = projectIterationDAO.getAllWordStatsStatistics(iteration.getId());
      }


      ContainerTranslationStatistics iterationStats = new ContainerTranslationStatistics();
      iterationStats.setId(iterationSlug);
      iterationStats.addRef( new Link(URI.create(zPathService.generatePathForProjectIteration(iteration)), "statSource", "PROJ_ITER"));

      for( String locale : transUnitIterationStats.keySet() )
      {
         if( locales.length == 0 || Arrays.binarySearch(locales, locale) >= 0)
         {
            TransUnitCount count = transUnitIterationStats.get( locale );

            // trans unit level stats
            TranslationStatistics transUnitStats = new TranslationStatistics();
            transUnitStats.setLocale( locale );
            transUnitStats.setUnit(TranslationStatistics.StatUnit.MESSAGE);
            transUnitStats.setTranslated( count.get(ContentState.Approved) );
            transUnitStats.setUntranslated(count.get(ContentState.New));
            transUnitStats.setNeedReview(count.get(ContentState.NeedReview));
            transUnitStats.setTotal( count.getTotal() );
            iterationStats.addStats(transUnitStats);

            // word level stats
            if( includeWordStats )
            {
               TransUnitWords wordCount = wordIterationStats.get(locale);

               TranslationStatistics wordStats = new TranslationStatistics();
               wordStats.setLocale( locale );
               wordStats.setUnit(TranslationStatistics.StatUnit.WORD);
               wordStats.setTranslated( wordCount.get(ContentState.Approved) );
               wordStats.setUntranslated( wordCount.get(ContentState.New) );
               wordStats.setNeedReview( wordCount.get(ContentState.NeedReview) );
               wordStats.setTotal( wordCount.getTotal() );
               iterationStats.addStats(wordStats);
            }
         }
      }

      // TODO Do in a single query
      if( includeDetails )
      {
         for( String docId : iteration.getDocuments().keySet() )
         {
            iterationStats.addDetailedStats(
                  this.getStatistics(projectSlug, iterationSlug, docId, includeWordStats, locales)
            );
         }
      }

      return iterationStats;
   }

   @Override
   public ContainerTranslationStatistics
   getStatistics(String projectSlug, String iterationSlug, String docId, boolean includeWordStats, String[] locales)
   {
      LocaleId[] localeIds = new LocaleId[ locales.length ];
      for( int i=0; i<locales.length; i++ )
      {
         localeIds[i] = new LocaleId( locales[i] );
      }

      HDocument document = documentDAO.getByProjectIterationAndDocId(projectSlug, iterationSlug, docId);

      if( document == null )
      {
         throw new NoSuchEntityException(projectSlug + "/" + iterationSlug + "/" + docId);
      }

      Map<LocaleId, TranslationStats> statsMap = documentDAO.getStatistics(document.getId(), localeIds);

      ContainerTranslationStatistics docStats = new ContainerTranslationStatistics();
      docStats.setId(docId);
      docStats.addRef(new Link(URI.create(zPathService.generatePathForDocument(document)), "statSource", "DOC"));

      for( LocaleId locale : statsMap.keySet() )
      {
         TranslationStats stats = statsMap.get( locale );
         TransUnitCount count = stats.getUnitCount();
         TransUnitWords wordCount = stats.getWordCount();

         // trans unit level stats
         TranslationStatistics transUnitStats = new TranslationStatistics();
         transUnitStats.setLocale( locale.getId() );
         transUnitStats.setUnit(TranslationStatistics.StatUnit.MESSAGE);
         transUnitStats.setTranslated( count.get(ContentState.Approved) );
         transUnitStats.setUntranslated(count.get(ContentState.New));
         transUnitStats.setNeedReview(count.get(ContentState.NeedReview));
         transUnitStats.setTotal( count.getTotal() );
         docStats.addStats(transUnitStats);

         // word level stats
         if( includeWordStats )
         {
            TranslationStatistics wordStats = new TranslationStatistics();
            wordStats.setLocale( locale.getId() );
            wordStats.setUnit(TranslationStatistics.StatUnit.WORD);
            wordStats.setTranslated( wordCount.get(ContentState.Approved) );
            wordStats.setUntranslated( wordCount.get(ContentState.New) );
            wordStats.setNeedReview( wordCount.get(ContentState.NeedReview) );
            wordStats.setTotal( wordCount.getTotal() );
            docStats.addStats(wordStats);
         }
      }

      return docStats;
   }
}
