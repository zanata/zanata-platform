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
package org.zanata.rest.service;

import static org.zanata.rest.service.SourceDocResource.RESOURCE_SLUG_TEMPLATE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.seam.resteasy.IgnoreInterfacePath;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.CopyTransService;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;

import com.google.common.base.Optional;

/**
 * This service allows clients to push and pull both source documents and translations.
 */
@Name("translatedDocResourceService")
@Path(TranslatedDocResourceService.SERVICE_PATH)
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Transactional
@IgnoreInterfacePath
public class TranslatedDocResourceService implements TranslatedDocResource
{

   // security actions
//   private static final String ACTION_IMPORT_TEMPLATE = "import-template";
//   private static final String ACTION_IMPORT_TRANSLATION = "import-translation";

   public static final String EVENT_COPY_TRANS = "org.zanata.rest.service.copyTrans";

   /** Project Identifier. */
   @PathParam("projectSlug")
   private String projectSlug;

   /** Project Iteration identifier. */
   @PathParam("iterationSlug")
   private String iterationSlug;

   /** (This parameter is optional and is currently not used) */
   @HeaderParam("Content-Type")
   @Context
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   @Context
   private UriInfo uri;

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
   private RestSlugValidator restSlugValidator;

   @In
   private TranslationService translationServiceImpl;

   private final Log log = Logging.getLog(TranslatedDocResourceService.class);

   @In
   private LocaleService localeServiceImpl;

   @Override
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response getTranslations(
         @PathParam("id") String idNoSlash,
         @PathParam("locale") LocaleId locale,
         @QueryParam("ext") Set<String> extensions,
         @QueryParam("skeletons") @DefaultValue("false") boolean skeletons,
         @HeaderParam(HttpHeaders.IF_NONE_MATCH) String eTag
         )
   {
      log.debug("start to get translation");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = restSlugValidator.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
      HLocale hLocale = restSlugValidator.validateTargetLocale(locale, projectSlug, iterationSlug);

      ResourceUtils.validateExtensions(extensions);

      // Check Etag header
      EntityTag generatedEtag = eTagUtils.generateETagForTranslatedDocument(hProjectIteration, id, hLocale);
      List<String> requestedEtagHeaders = headers.getRequestHeader(HttpHeaders.IF_NONE_MATCH);
      if( requestedEtagHeaders != null && !requestedEtagHeaders.isEmpty() )
      {
         if( requestedEtagHeaders.get(0).equals(generatedEtag.getValue()) )
         {
            return Response.notModified(generatedEtag).build();
         }
      }

      ResponseBuilder response = request.evaluatePreconditions(generatedEtag);
      if (response != null)
      {
         return response.build();
      }

      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, id);
      if (document.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).build();
      }

      TranslationsResource translationResource = new TranslationsResource();
      // TODO avoid queries for better cacheability
      List<HTextFlowTarget> hTargets = textFlowTargetDAO.findTranslations(document, hLocale);
      boolean foundData = resourceUtils.transferToTranslationsResource(
            translationResource, document, hLocale, extensions, hTargets, Optional.<String>absent());

      if (!foundData && !skeletons)
      {
         return Response.status(Status.NOT_FOUND).build();
      }

      // TODO lastChanged
      return Response.ok().entity(translationResource).tag(generatedEtag).build();
   }

   @Override
   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   @Restrict("#{s:hasPermission(translatedDocResourceService.securedIteration.project, 'modify-translation')}")
   // /r/{id}/translations/{locale}
   public Response deleteTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale)
   {
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = restSlugValidator.retrieveAndCheckIteration(projectSlug, iterationSlug, true);
      HLocale hLocale = restSlugValidator.validateTargetLocale(locale, projectSlug, iterationSlug);

      // TODO find correct etag
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, new HashSet<String>());

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, id);
      if (document.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      List<HTextFlowTarget> targets = textFlowTargetDAO.findAllTranslations(document, locale);

      for (HTextFlowTarget target : targets)
      {
         target.clear();
      }

      // we also need to delete the extensions here
      document.getPoTargetHeaders().remove(hLocale);
      textFlowTargetDAO.flush();

      return Response.ok().build();

   }

   @Override
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response putTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, TranslationsResource messageBody, @QueryParam("ext") Set<String> extensions, @QueryParam("merge") @DefaultValue("auto") String merge)
   {
      // check security (cannot be on @Restrict as it refers to method parameters)
      identity.checkPermission("modify-translation", this.localeServiceImpl.getByLocaleId(locale),
            this.getSecuredIteration().getProject());

      log.debug("start put translations");
      MergeType mergeType;
      try
      {
         mergeType = MergeType.valueOf(merge.toUpperCase());
      }
      catch (Exception e)
      {
         return Response.status(Status.BAD_REQUEST).entity("bad merge type "+merge).build();
      }
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);

      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      // TODO create valid etag
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, new HashSet<String>(0));

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      // Translate
      List<String> warnings =
         this.translationServiceImpl.translateAllInDoc(projectSlug, iterationSlug, id, locale, messageBody, extensions, mergeType);

      // Regenerate etag in case it has changed
      // TODO create valid etag
      etag = eTagUtils.generateETagForDocument(hProjectIteration, id, new HashSet<String>(0));

      log.debug("successful put translation");
      // TODO lastChanged
      StringBuilder sb = new StringBuilder();
      for (String warning : warnings)
      {
         sb.append("warning: ").append(warning).append('\n');
      }
      return Response.ok(sb.toString()).tag(etag).build();
   }

   // TODO investigate how this became dead code, then delete
   public void copyClosestEquivalentTranslation(HDocument document)
   {
      if (applicationConfiguration.getEnableCopyTrans())
      {
         copyTransServiceImpl.copyTransForDocument(document);
      }
   }

   // public for Seam's benefit (and the @Restrict in deleteTranslations)
   public HProjectIteration getSecuredIteration()
   {
      return restSlugValidator.retrieveAndCheckIteration(projectSlug, iterationSlug, false);
   }

}
