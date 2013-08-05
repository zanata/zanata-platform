/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.rest.service;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowStreamingDAO;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.dao.TransMemoryStreamingDAO;
import org.zanata.exception.EntityMissingException;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.ITextFlow;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.service.LocaleService;
import org.zanata.tmx.TMXParser;
import org.zanata.util.CloseableIterator;
import com.google.common.base.Optional;

@Name("translationMemoryResourceService")
@Path("/tm")
@Transactional(TransactionPropagationType.SUPPORTS)
@Slf4j
// TODO options to export obsolete docs and textflows to TMX?
public class TranslationMemoryResourceService implements TranslationMemoryResource
{

   @In
   private LocaleService localeServiceImpl;
   @In
   private RestSlugValidator restSlugValidator;
   @In
   private TextFlowStreamingDAO textFlowStreamDAO;
   @In
   private TransMemoryStreamingDAO transMemoryStreamingDAO;
   @In
   private TransMemoryDAO transMemoryDAO;
   @In
   private TMXParser tmxParser;

   @Override
   @Restrict("#{s:hasRole('admin')}")
   public Response getAllTranslationMemory(@Nullable LocaleId locale)
   {
      log.debug("exporting TMX for all projects, locale {}", locale);
      if (locale != null)
      {
         localeServiceImpl.validateSourceLocale(locale);
         // TODO findTextFlowsByLocale
      }
      String filename = makeTMXFilename(null, null, locale);
      CloseableIterator<HTextFlow> iter = textFlowStreamDAO.findTextFlows();
      return buildTMX(iter, locale, filename);
   }

   @Override
   public Response getProjectTranslationMemory(@Nonnull String projectSlug, @Nullable LocaleId locale)
   {
      log.debug("exporting TMX for project {}, locale {}", projectSlug, locale);
      HProject hProject = restSlugValidator.retrieveAndCheckProject(projectSlug, false);
      if (locale != null)
      {
         restSlugValidator.validateTargetLocale(locale, projectSlug);
         // TODO findTextFlowsByProjectAndLocale
      }
      String filename = makeTMXFilename(projectSlug, null, locale);
      CloseableIterator<HTextFlow> iter = textFlowStreamDAO.findTextFlowsByProject(hProject);
      return buildTMX(iter, locale, filename);
   }

   @Override
   public Response getProjectIterationTranslationMemory(
         @Nonnull String projectSlug, @Nonnull String iterationSlug, @Nullable LocaleId locale)
   {
      log.debug("exporting TMX for project {}, iteration {}, locale {}", projectSlug, iterationSlug, locale);
      HProjectIteration hProjectIteration = restSlugValidator.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
      if (locale != null)
      {
         restSlugValidator.validateTargetLocale(locale, projectSlug, iterationSlug);
         // TODO findTextFlowsByProjectIterationAndLocale
      }
      String filename = makeTMXFilename(projectSlug, iterationSlug, locale);
      CloseableIterator<HTextFlow> iter = textFlowStreamDAO.findTextFlowsByProjectIteration(hProjectIteration);
      return buildTMX(iter, locale, filename);
   }

   @Override
   public Response getTranslationMemory(@Nonnull String slug)
   {
      log.debug("exporting TMX for translation memory {}", slug);
      TransMemory tm = getTM(transMemoryDAO.getBySlug(slug), slug);
      String filename = makeTMXFilename(slug);
      CloseableIterator<TransMemoryUnit> iter = transMemoryStreamingDAO.findTransUnitsByTM(tm);
      return buildTMX(tm, iter, filename);
   }

   @Override
   @Restrict("#{s:hasRole('admin')}")
   public Response updateTranslationMemory(String slug, InputStream input) throws Exception
   {
      Optional<TransMemory> tm = transMemoryDAO.getBySlug(slug);
      tmxParser.parseAndSaveTMX(input, getTM(tm, slug));
      return Response.status(200).build();
   }

   private TransMemory getTM(Optional<TransMemory> tm, String slug)
   {
      if (!tm.isPresent())
      {
         throw new EntityMissingException("Translation memory " + slug + " was not found.");
      }
      return tm.get();
   }

   @Override
   @Restrict("#{s:hasRole('admin')}")
   public Response deleteTranslationUnits(String slug)
   {
      transMemoryDAO.deleteTransMemoryContents(slug);
      return Response.ok().build();
   }

   private Response buildTMX(
         @Nonnull CloseableIterator<? extends ITextFlow> iter,
         @Nullable LocaleId locale, @Nonnull String filename)
   {
      TMXStreamingOutput<HTextFlow> output = new TMXStreamingOutput(iter, new TranslationsTMXExportStrategy(locale));
      return okResponse(filename, output);
   }

   private Response buildTMX(
         TransMemory tm, CloseableIterator<TransMemoryUnit> iter, String filename)
   {
      TMXStreamingOutput<TransMemoryUnit> output = new TMXStreamingOutput<TransMemoryUnit>(iter, new TransMemoryTMXExportStrategy(tm));
      return okResponse(filename, output);
   }

   private Response okResponse(String filename, StreamingOutput output)
   {
      return Response.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .type(PREFERRED_MEDIA_TYPE)
            .entity(output).build();
   }

   private static @Nonnull String makeTMXFilename(@Nullable String projectSlug, @Nullable String iterationSlug, @Nullable LocaleId locale)
   {
      String p = projectSlug != null ? projectSlug : "allProjects";
      String i = iterationSlug != null ? iterationSlug : "allVersions";
      String l = locale != null ? locale.getId() : "allLocales";
      return "zanata-"+p+"-"+i+"-"+l+".tmx";
   }

   private static @Nonnull String makeTMXFilename(@Nullable String tmSlug)
   {
      return "zanata-"+tmSlug+".tmx";
   }

}
