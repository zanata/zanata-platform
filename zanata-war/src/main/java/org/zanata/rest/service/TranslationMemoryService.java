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

import java.util.Collection;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;

@Name("translationMemoryService")
@Path("tm")
@Transactional
public class TranslationMemoryService implements TranslationMemoryResource
{

   // security actions
//   private static final String ACTION_IMPORT_TM = "import-tm";
//   private static final String ACTION_EXPORT_TM = "export-tm";

   @In
   private ZanataIdentity identity;

   @In
   private ApplicationConfiguration applicationConfiguration;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private ProjectDAO projectDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In
   private ResourceUtils resourceUtils;

   @In
   private ETagUtils eTagUtils;
   
   @In
   private CopyTransService copyTransServiceImpl;

   @In
   private ProjectService projectService;

   @In
   private ProjectIterationService projectIterationService;

   @In
   private TranslationService translationServiceImpl;

   private final Log log = Logging.getLog(TranslationMemoryService.class);

   @In
   private LocaleService localeServiceImpl;

   @Override
   @GET
   public Response getAllTranslationMemory(@QueryParam("locale") LocaleId locale)
   {
      log.debug("exporting TM for all projects, locale {0}", locale);
      // TODO security checks, etag
      // TODO Auto-generated method stub
      if (locale != null)
      {
         localeServiceImpl.validateSourceLocale(locale);
      }
      String filename = makeTMXFilename(null, null, locale);
      Object output = null;
      return Response.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .type(PREFERRED_MEDIA_TYPE)
            .entity(output).build();
   }

   @Override
   @GET
   public Response getProjectTranslationMemory(@PathParam("projectSlug") @Nonnull String projectSlug, @QueryParam("locale") LocaleId locale)
   {
      log.debug("exporting TM for project {0}, locale {1}", projectSlug, locale);
      // TODO security checks, etag
      // TODO Auto-generated method stub
      projectService.retrieveAndCheckProject(projectSlug, false);
      if (locale != null)
      {
         projectService.validateTargetLocale(locale, projectSlug);
      }

      String filename = makeTMXFilename(projectSlug, null, locale);
      Object output = null;
      return Response.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .type(PREFERRED_MEDIA_TYPE)
            .entity(output).build();
   }

   @Override
   @GET
   public Response getProjectIterationTranslationMemory(
         @Nonnull String projectSlug, @Nonnull String iterationSlug, LocaleId locale)
   {
      log.debug("exporting TM for project {0}, iteration {1}, locale {2}", projectSlug, iterationSlug, locale);
      // TODO security checks, etag
      HProjectIteration hProjectIteration = projectIterationService.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
      if (locale != null)
      {
         projectIterationService.validateTargetLocale(locale, projectSlug, iterationSlug);
      }

      // TODO option to export obsolete docs to TMX?
      Collection<HDocument> documents = hProjectIteration.getDocuments().values();
      Iterator<HDocument> docIter = documents.iterator();
      HLocale sourceLocale;
      if (docIter.hasNext())
      {
         sourceLocale = docIter.next().getLocale();
      }
      else
      {
         // TODO return empty TMX?
         throw new WebApplicationException(404); // no docs
      }

      StreamingOutput output = new TMXStreamingOutput(hProjectIteration, sourceLocale.getLocaleId(), locale);
      String filename = makeTMXFilename(projectSlug, iterationSlug, locale);
      return Response.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .type(PREFERRED_MEDIA_TYPE)
            .entity(output).build();
   }

   private static String makeTMXFilename(String projectSlug, String iterationSlug, LocaleId locale)
   {
      String p = projectSlug != null ? projectSlug : "allProjects";
      String i = iterationSlug != null ? iterationSlug : "allVersions";
      String l = locale != null ? locale.getId() : "allLocales";
      return "zanata-"+p+"-"+i+"-"+l+".tmx";
   }

}
