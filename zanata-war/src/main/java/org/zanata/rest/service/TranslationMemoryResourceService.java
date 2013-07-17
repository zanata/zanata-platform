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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.Suspend;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.spi.AsynchronousResponse;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.TransactionPropagationType;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowStreamDAO;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.SourceContents;
import org.zanata.service.LocaleService;

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

   @Override
   @Restrict("#{s:hasRole('admin')}")
   public Response getAllTranslationMemory(@Nullable LocaleId locale)
   {
      TranslationMemoryResourceService.log.debug("exporting TM for all projects, locale {}", locale);
      Iterator<? extends SourceContents> tuIter;
      if (locale != null)
      {
         localeServiceImpl.validateSourceLocale(locale);
         // TODO findTextFlowsByLocale
      }
      tuIter = textFlowStreamDAO.findTextFlows();
      return buildTMX(tuIter, null, null, locale);
   }

   @Override
   public Response getProjectTranslationMemory(@Nonnull String projectSlug, @Nullable LocaleId locale)
   {
      TranslationMemoryResourceService.log.debug("exporting TM for project {}, locale {}", projectSlug, locale);
      Iterator<? extends SourceContents> tuIter;
      HProject hProject = restSlugValidator.retrieveAndCheckProject(projectSlug, false);
      if (locale != null)
      {
         restSlugValidator.validateTargetLocale(locale, projectSlug);
         // TODO findTextFlowsByProjectAndLocale
      }
      tuIter = textFlowStreamDAO.findTextFlowsByProject(hProject);
      return buildTMX(tuIter, projectSlug, null, locale);
   }

   @Override
   public Response getProjectIterationTranslationMemory(
         @Nonnull String projectSlug, @Nonnull String iterationSlug, @Nullable LocaleId locale)
   {
      TranslationMemoryResourceService.log.debug("exporting TM for project {}, iteration {}, locale {}", projectSlug, iterationSlug, locale);
      Iterator<? extends SourceContents> tuIter;
      HProjectIteration hProjectIteration = restSlugValidator.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
      if (locale != null)
      {
         restSlugValidator.validateTargetLocale(locale, projectSlug, iterationSlug);
         // TODO findTextFlowsByProjectIterationAndLocale
      }
      tuIter = textFlowStreamDAO.findTextFlowsByProjectIteration(hProjectIteration);
      return buildTMX(tuIter, projectSlug, iterationSlug, locale);
   }

   @POST
   @Path("/{slug}")
   @Consumes(MediaType.MULTIPART_FORM_DATA)
   public Response updateTranslationMemory(@PathParam("slug") String slug, MultipartFormDataInput input) throws Exception
   {
      for(InputPart inputPart : input.getFormDataMap().get("uploadedFile"))
      {
         InputStream inputStream = inputPart.getBody(InputStream.class, null);

         byte [] bytes = IOUtils.toByteArray(inputStream);
         System.out.println(new String(bytes));
      }
      return Response.status(200).build();
   }

   @GET
   @Path("basic")
   @Produces("text/plain")
   public void getBasic(final @Suspend(Long.MAX_VALUE) AsynchronousResponse response) throws Exception
   {
      Thread t = new Thread(new Runnable()
      {
         @Override
         public void run()
         {
            int i = 0;
            while(i<60)
            {
               try
               {
                  Thread.sleep(10000);
               } catch (InterruptedException e)
               {
               }
               i++;
               System.out.println("Waiting for " + (i*10) + " seconds");
            }
            response.setResponse(Response.ok("Finished").build());
         }
      });
      t.start();
   }

   private Response buildTMX(@Nonnull Iterator<? extends SourceContents> tuIter, @Nullable String projectSlug, @Nullable String iterationSlug, @Nullable LocaleId locale)
   {
      StreamingOutput output = new TMXStreamingOutput(tuIter, locale);
      String filename = makeTMXFilename(projectSlug, iterationSlug, locale);
      return Response.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .type(PREFERRED_MEDIA_TYPE)
            .entity(output).build();
   }

   private static String makeTMXFilename(@Nullable String projectSlug, @Nullable String iterationSlug, @Nullable LocaleId locale)
   {
      String p = projectSlug != null ? projectSlug : "allProjects";
      String i = iterationSlug != null ? iterationSlug : "allVersions";
      String l = locale != null ? locale.getId() : "allLocales";
      return "zanata-"+p+"-"+i+"-"+l+".tmx";
   }

}
