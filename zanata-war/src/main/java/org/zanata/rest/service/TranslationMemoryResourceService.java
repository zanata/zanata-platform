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
import java.util.Iterator;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowStreamDAO;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.dao.TransMemoryStreamingDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.SourceContents;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.service.LocaleService;
import org.zanata.tmx.TMXParser;

import lombok.extern.slf4j.Slf4j;

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
   private TextFlowStreamDAO textFlowStreamDAO;
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
      Iterator<HTextFlow> tuIter;
      if (locale != null)
      {
         localeServiceImpl.validateSourceLocale(locale);
         // TODO findTextFlowsByLocale
      }
      tuIter = textFlowStreamDAO.findTextFlows();
      String filename = makeTMXFilename(null, null, locale);
      return buildTMX(tuIter, locale, filename);
   }

   @Override
   public Response getProjectTranslationMemory(@Nonnull String projectSlug, @Nullable LocaleId locale)
   {
      log.debug("exporting TMX for project {}, locale {}", projectSlug, locale);
      Iterator<HTextFlow> tuIter;
      HProject hProject = restSlugValidator.retrieveAndCheckProject(projectSlug, false);
      if (locale != null)
      {
         restSlugValidator.validateTargetLocale(locale, projectSlug);
         // TODO findTextFlowsByProjectAndLocale
      }
      tuIter = textFlowStreamDAO.findTextFlowsByProject(hProject);
      String filename = makeTMXFilename(projectSlug, null, locale);
      return buildTMX(tuIter, locale, filename);
   }

   @Override
   public Response getProjectIterationTranslationMemory(
         @Nonnull String projectSlug, @Nonnull String iterationSlug, @Nullable LocaleId locale)
   {
      log.debug("exporting TMX for project {}, iteration {}, locale {}", projectSlug, iterationSlug, locale);
      Iterator<HTextFlow> tuIter;
      HProjectIteration hProjectIteration = restSlugValidator.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
      if (locale != null)
      {
         restSlugValidator.validateTargetLocale(locale, projectSlug, iterationSlug);
         // TODO findTextFlowsByProjectIterationAndLocale
      }
      tuIter = textFlowStreamDAO.findTextFlowsByProjectIteration(hProjectIteration);
      String filename = makeTMXFilename(projectSlug, iterationSlug, locale);
      return buildTMX(tuIter, locale, filename);
   }

   @Override
   public Response getTranslationMemory(@Nonnull String slug)
   {
      log.debug("exporting TMX for translation memory {}", slug);
      Iterator<TMTranslationUnit> tuIter;
      TransMemory transMemory = transMemoryDAO.getBySlug(slug);
      if (transMemory == null)
      {
         throw new ZanataServiceException("Translation memory " + slug + " was not found.", 404);
      }
      tuIter = transMemoryStreamingDAO.findTransUnitsByTM(transMemory);
      String filename = makeTMXFilename(slug);
      return buildTMX(tuIter, filename);
   }

   @Override
   @Restrict("#{s:hasRole('admin')}")
   public Response updateTranslationMemory(String slug, MultipartFormDataInput input) throws Exception
   {
      for(InputPart inputPart : input.getFormDataMap().get("uploadedFile"))
      {
         InputStream inputStream = inputPart.getBody(InputStream.class, null);

         tmxParser.parseAndSaveTMX(inputStream, transMemoryDAO.getBySlug(slug));
      }
      return Response.status(200).build();
   }

   @Override
   @Restrict("#{s:hasRole('admin')}")
   public Response deleteTranslationUnits(String slug)
   {
      transMemoryDAO.deleteTransMemoryContents(slug);
      return Response.ok().build();
   }

   private Response buildTMX(
         @Nonnull Iterator<? extends SourceContents> tuIter,
         @Nullable LocaleId locale, @Nonnull String filename)
   {
      TMXStreamingOutput<HTextFlow> output = new TMXStreamingOutput(tuIter, new ExportSourceContentsStrategy(locale));
      return okResponse(filename, output);
   }

   private Response buildTMX(Iterator<TMTranslationUnit> tuIter, String filename)
   {
      TMXStreamingOutput<TMTranslationUnit> output = new TMXStreamingOutput<TMTranslationUnit>(tuIter, new ExportTransUnitStrategy());
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
