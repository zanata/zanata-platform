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
import javax.ws.rs.core.Response.Status;
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
import org.zanata.exception.ZanataServiceException;
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

   /** Project Identifier. */
   @PathParam("projectSlug")
   private String projectSlug;

   /** Project Iteration identifier. */
   @PathParam("iterationSlug")
   private String iterationSlug;

//   /** (This parameter is optional and is currently not used) */
//   @HeaderParam("Content-Type")
//   @Context
//   private MediaType requestContentType;
//
//   @Context
//   private HttpHeaders headers;
//
//   @Context
//   private Request request;
//
//   @Context
//   private UriInfo uri;

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
      // TODO Auto-generated method stub
      return null;
   }

   // TODO duplicated code from TranslatedDocResourceService
   private @Nonnull HLocale validateTargetLocale(LocaleId locale, String projectSlug, String iterationSlug)
   {
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, projectSlug, iterationSlug);
         return hLocale;
      }
      catch (ZanataServiceException e)
      {
         log.warn("Exception validating target locale {0} in proj {1} iter {2}", e, locale, projectSlug, iterationSlug);
         throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(e.getMessage()).build());
      }
   }

   @Override
   @GET
   public Response getProjectTranslationMemory(@PathParam("projectSlug") String projectSlug, @QueryParam("locale") LocaleId locale)
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   @GET
   public Response getProjectIterationTranslationMemory(
         String projectSlug, String iterationSlug, LocaleId locale)
   {
      log.debug("start to get TM");
      // TODO security checks
      HProjectIteration hProjectIteration = projectIterationService.retrieveAndCheckIteration(false);
      if (locale != null)
      {
         validateTargetLocale(locale, projectSlug, iterationSlug);
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
      String filename = makeFilename(projectSlug, iterationSlug, locale);
      return Response.ok()
            .header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
            .type(PREFERRED_MEDIA_TYPE)
            .entity(output).build();
   }

   private static String makeFilename(String projectSlug, String iterationSlug, LocaleId locale)
   {
      String p = projectSlug != null ? projectSlug : "allProjects";
      String i = iterationSlug != null ? iterationSlug : "allVersions";
      String l = locale != null ? locale.getId() : "allLocales";
      return "zanata-"+p+"-"+i+"-"+l+".tmx";
   }

}
