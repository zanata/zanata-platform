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

import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;


import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.core.Events;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.zanata.ZanataInit;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.TranslationResourcesResource;
import org.zanata.service.LocaleService;

import com.google.common.collect.Sets;

@Name("translationResourcesService")
@Path(TranslationResourcesService.SERVICE_PATH)
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Transactional
public class TranslationResourcesService implements TranslationResourcesResource
{

   // security actions
   private static final String ACTION_IMPORT_TEMPLATE = "import-template";
   private static final String ACTION_IMPORT_TRANSLATION = "import-translation";

   public static final String SERVICE_PATH = ProjectIterationService.SERVICE_PATH + "/r";

   public static final String EVENT_COPY_TRANS = "org.zanata.rest.service.copyTrans";
   
   @PathParam("projectSlug")
   private String projectSlug;

   @PathParam("iterationSlug")
   private String iterationSlug;

   @QueryParam("ext")
   @DefaultValue("")
   private Set<String> extensions;
   
   @QueryParam("copyTrans")
   @DefaultValue("true")
   private boolean copytrans;


   @HeaderParam("Content-Type")
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   @Context
   private UriInfo uri;

   @In
   private ZanataInit zanataInit;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private TextFlowDAO textFlowDAO;

   @In
   private TextFlowTargetDAO textFlowTargetDAO;

   @In
   private ResourceUtils resourceUtils;

   @In
   Identity identity;

   @In
   private ETagUtils eTagUtils;

   @In
   private PersonDAO personDAO;

   @In
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

   private final Log log = Logging.getLog(TranslationResourcesService.class);

   @In
   private LocaleService localeServiceImpl;


   public TranslationResourcesService()
   {
   }

   // TODO break up this class (too many responsibilities)
   public TranslationResourcesService(ProjectIterationDAO projectIterationDAO, DocumentDAO documentDAO, PersonDAO personDAO, TextFlowTargetDAO textFlowTargetDAO, LocaleService localeService, ResourceUtils resourceUtils, Identity identity, ETagUtils eTagUtils)
   {
      this.projectIterationDAO = projectIterationDAO;
      this.documentDAO = documentDAO;
      this.personDAO = personDAO;
      this.textFlowTargetDAO = textFlowTargetDAO;
      this.localeServiceImpl = localeService;
      this.resourceUtils = resourceUtils;
      this.identity = identity;
      this.eTagUtils = eTagUtils;
   }

   @Override
   @HEAD
   public Response head()
   {
      HProjectIteration hProjectIteration = retrieveIteration();
      validateExtensions();
      EntityTag etag = projectIterationDAO.getResourcesETag(hProjectIteration);
      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }
      return Response.ok().tag(etag).build();
   }

   /**
    * Retrieve the List of Resources
    * 
    * @return Response.ok with ResourcesList or Response(404) if not found
    */
   @Override
   @GET
   @Wrapped(element = "resources", namespace = Namespaces.ZANATA_API)
   public Response get()
   {

      HProjectIteration hProjectIteration = retrieveIteration();

      EntityTag etag = projectIterationDAO.getResourcesETag(hProjectIteration);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      List<ResourceMeta> resources = new ArrayList<ResourceMeta>();

      for (HDocument doc : hProjectIteration.getDocuments().values())
      {
         // TODO we shouldn't need this check
         if (!doc.isObsolete())
         {
            ResourceMeta resource = new ResourceMeta();
            resourceUtils.transferToAbstractResourceMeta(doc, resource);
            resources.add(resource);
         }
      }

      Type genericType = new GenericType<List<ResourceMeta>>()
      {
      }.getGenericType();
      Object entity = new GenericEntity<List<ResourceMeta>>(resources, genericType);
      return Response.ok(entity).tag(etag).build();
   }


   @Override
   @POST
   public Response post(InputStream messageBody)
   {
      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TEMPLATE);

      validateExtensions(PoHeader.ID, PotEntryHeader.ID);

      Resource entity = RestUtils.unmarshall(Resource.class, messageBody, requestContentType, headers.getRequestHeaders());
      HDocument document = documentDAO.getByDocId(hProjectIteration, entity.getName());
      HLocale hLocale = validateSourceLocale(entity.getLang());
      int nextDocRev;
      if (document != null)
      {
         if (!document.isObsolete())
         {
            // updates must happen through PUT on the actual resource
            return Response.status(Status.CONFLICT).entity("A document with name " + entity.getName() + " already exists.").build();
         }
         // a deleted document is being created again
         nextDocRev = document.getRevision() + 1;
         document.setObsolete(false);
      }
      else
      {
         nextDocRev = 1;
         document = new HDocument(entity.getName(), entity.getContentType(), hLocale);
         document.setProjectIteration(hProjectIteration);
      }
      hProjectIteration.getDocuments().put(entity.getName(), document);
      
      resourceUtils.transferFromResource(entity, document, extensions, hLocale, nextDocRev);

      document = documentDAO.makePersistent(document);
      documentDAO.flush();
      
      if (copytrans && nextDocRev == 1)
      {
         copyClosestEquivalentTranslation(document.getId(), entity.getName(), projectSlug, iterationSlug);
      }
           
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, document.getDocId(), extensions);

      return Response.created(URI.create("r/" + resourceUtils.encodeDocId(document.getDocId()))).tag(etag).build();
   }

   @Override
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response getResource(@PathParam("id") String idNoSlash)
   {
      log.debug("start get resource");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();

      validateExtensions(PoHeader.ID, PotEntryHeader.ID);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

      if (doc == null || doc.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).entity("document not found").build();
      }

      Resource entity = new Resource(doc.getDocId());
      log.debug("get resource details {0}", entity.toString());
      resourceUtils.transferToResource(doc, entity);

      for (HTextFlow htf : doc.getTextFlows())
      {
         TextFlow tf = new TextFlow(htf.getResId(), doc.getLocale().getLocaleId());
         resourceUtils.transferToTextFlow(htf, tf);
         resourceUtils.transferToTextFlowExtensions(htf, tf.getExtensions(true), extensions);
         entity.getTextFlows().add(tf);
      }

      // handle extensions
      resourceUtils.transferToResourceExtensions(doc, entity.getExtensions(true), extensions);
      log.debug("Get resource :{0}", entity.toString());
      return Response.ok().entity(entity).tag(etag).lastModified(doc.getLastChanged()).build();
   }

   private HLocale validateTargetLocale(LocaleId locale, String projectSlug, String iterationSlug)
   {
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.validateLocaleByProjectIteration(locale, projectSlug, iterationSlug);
         return hLocale;
      }
      catch (ZanataServiceException e)
      {
         throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(e.getMessage()).build());
      }
   }

   private HLocale validateSourceLocale(LocaleId locale)
   {
      try
      {
         return localeServiceImpl.validateSourceLocale(locale);
      }
      catch (ZanataServiceException e)
      {
         throw new WebApplicationException(Response.status(Status.FORBIDDEN).entity(e.getMessage()).build());
      }
   }

   @Override
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response putResource(@PathParam("id") String idNoSlash, InputStream messageBody)
   {
      log.debug("start put resource");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      ResponseBuilder response;
      EntityTag etag = null;
      boolean changed = false;
      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TEMPLATE);

      validateExtensions();

      Resource entity = RestUtils.unmarshall(Resource.class, messageBody, requestContentType, headers.getRequestHeaders());
      log.debug("resource details: {0}", entity);

      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
      HLocale hLocale = validateSourceLocale(entity.getLang());
      int nextDocRev;
      if (document == null)
      { // must be a create operation
         nextDocRev = 1;
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }
         changed = true;
         document = new HDocument(entity.getName(), entity.getContentType(), hLocale);
         document.setProjectIteration(hProjectIteration);
         hProjectIteration.getDocuments().put(id, document);
         response = Response.created(uri.getAbsolutePath());
      }
      else if (document.isObsolete())
      { // must also be a create operation
         nextDocRev = document.getRevision() + 1;
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }
         changed = true;
         document.setObsolete(false);
         // not sure if this is needed
         hProjectIteration.getDocuments().put(id, document);
         response = Response.created(uri.getAbsolutePath());
      }
      else
      { // must be an update operation
         nextDocRev = document.getRevision() + 1;
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
         response = request.evaluatePreconditions(etag);
         if (response != null)
         {
            return response.build();
         }

         response = Response.ok();
      }

      changed |= resourceUtils.transferFromResource(entity, document, extensions, hLocale, nextDocRev);


      if (changed)
      {
         document = documentDAO.makePersistent(document);
         documentDAO.flush();
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
      }


      if (copytrans && nextDocRev == 1)
      {
         copyClosestEquivalentTranslation(document.getId(), entity.getName(), projectSlug, iterationSlug);
      }
            
      log.debug("put resource successfully");
      return response.tag(etag).build();

   }

   @Override
   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response deleteResource(@PathParam("id") String idNoSlash)
   {
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TEMPLATE);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
      document.setObsolete(true);
      documentDAO.flush();
      return Response.ok().build();
   }

   @Override
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response getResourceMeta(@PathParam("id") String idNoSlash)
   {
      log.debug("start to get resource meta");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument doc = documentDAO.getByDocId(hProjectIteration, id);

      if (doc == null)
      {
         return Response.status(Status.NOT_FOUND).entity("document not found").build();
      }

      ResourceMeta entity = new ResourceMeta(doc.getDocId());
      resourceUtils.transferToAbstractResourceMeta(doc, entity);

      // transfer extensions
      resourceUtils.transferToResourceExtensions(doc, entity.getExtensions(true), extensions);

      log.debug("successfuly get resource meta: {0}" , entity);
      return Response.ok().entity(entity).tag(etag).build();
   }

   @Override
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response putResourceMeta(@PathParam("id") String idNoSlash, InputStream messageBody)
   {
      log.debug("start to put resource meta");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TEMPLATE);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      log.debug("pass evaluation");
      ResourceMeta entity = RestUtils.unmarshall(ResourceMeta.class, messageBody, requestContentType, headers.getRequestHeaders());
      log.debug("put resource meta: {0}", entity);

      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
      if (document == null)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      if (document.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      HLocale hLocale = validateTargetLocale(entity.getLang(), projectSlug, iterationSlug);
      boolean changed = resourceUtils.transferFromResourceMetadata(entity, document, extensions, hLocale, document.getRevision() + 1);

      if (changed)
      {
         documentDAO.flush();
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
      }

      log.debug("put resource meta successfully");
      return Response.ok().tag(etag).lastModified(document.getLastChanged()).build();

   }

   @Override
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response getTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale)
   {
      log.debug("start to get translation");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();

      validateExtensions();

      // TODO create valid etag
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
      if (document.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).build();
      }

      HLocale hLocale = validateTargetLocale(locale, projectSlug, iterationSlug);

      List<HTextFlowTarget> hTargets = textFlowTargetDAO.findTranslations(document, locale);
      TranslationsResource translationResource = new TranslationsResource();
      resourceUtils.transferToTranslationsResourceExtensions(document, translationResource.getExtensions(true), extensions, hLocale, hTargets);

      if (hTargets.isEmpty() && translationResource.getExtensions(true).isEmpty())
      {
         return Response.status(Status.NOT_FOUND).build();
      }

      for (HTextFlowTarget hTarget : hTargets)
      {
         TextFlowTarget target = new TextFlowTarget();
         target.setResId(hTarget.getTextFlow().getResId());
         resourceUtils.transferToTextFlowTarget(hTarget, target);
         resourceUtils.transferToTextFlowTargetExtensions(hTarget, target.getExtensions(true), extensions);
         translationResource.getTextFlowTargets().add(target);
      }

      // TODO lastChanged
      return Response.ok().entity(translationResource).tag(etag).build();

   }

   @Override
   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response deleteTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale)
   {
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();
      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TRANSLATION);

      // TODO find correct etag
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
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
      document.getPoTargetHeaders().remove(locale);
      textFlowTargetDAO.flush();

      return Response.ok().build();

   }

   @Override
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response putTranslations(@PathParam("id") String idNoSlash, @PathParam("locale") LocaleId locale, @QueryParam("merge") @DefaultValue("auto") String _merge, InputStream messageBody)
   {
      log.debug("start put translations");
      MergeType mergeType;
      try
      {
         mergeType = MergeType.valueOf(_merge.toUpperCase());
      }
      catch (Exception e)
      {
         return Response.status(Status.BAD_REQUEST).entity("bad merge type "+_merge).build();
      }
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TRANSLATION);

      validateExtensions();

      // TODO create valid etag
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      log.debug("pass evaluate");
      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
      if (document.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).build();
      }


      TranslationsResource entity = RestUtils.unmarshall(TranslationsResource.class, messageBody, requestContentType, headers.getRequestHeaders());
      log.debug("start put translations entity:{0}" , entity);

      boolean changed = false;

      HLocale hLocale = validateTargetLocale(locale, projectSlug, iterationSlug);
      // handle extensions
      changed |= resourceUtils.transferFromTranslationsResourceExtensions(entity.getExtensions(true), document, extensions, hLocale, mergeType);

      List<HPerson> newPeople = new ArrayList<HPerson>();
      // NB: removedTargets only applies for MergeType.IMPORT
      Collection<HTextFlowTarget> removedTargets = new HashSet<HTextFlowTarget>();
      Collection<String> unknownResIds = new LinkedHashSet<String>();

      if (mergeType == MergeType.IMPORT)
      {
         for (HTextFlow textFlow : document.getTextFlows())
         {
            HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
            if (hTarget != null)
            {
               removedTargets.add(hTarget);
            }
         }
      }

      for (TextFlowTarget current : entity.getTextFlowTargets())
      {
         String resId = current.getResId();
         HTextFlow textFlow = textFlowDAO.getById(document, resId);
         if (textFlow == null)
         {
            // return warning for unknown resId to REST client
            unknownResIds.add(resId);
            log.warn("skipping TextFlowTarget with unknown resId: {0}", resId);
            continue;
         }
         else
         {
            if (current.getContent().isEmpty() && current.getState() != ContentState.New)
            {
               return Response.status(Status.BAD_REQUEST).entity("empty TextFlowTarget " + current.getResId() + " must have ContentState New").build();
            }
            if (current.getState() == ContentState.New && !current.getContent().isEmpty())
            {
               return Response.status(Status.BAD_REQUEST).entity("ContentState New is illegal for non-empty TextFlowTarget " + current.getResId()).build();
            }

            HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
            boolean targetChanged = false;
            if (hTarget == null)
            {
               targetChanged = true;
               log.debug("locale: {0}", locale);
               hTarget = new HTextFlowTarget(textFlow, hLocale);
               hTarget.setVersionNum(0); // incremented when content is set
               textFlow.getTargets().put(hLocale, hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTarget(current, hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(current.getExtensions(true), hTarget, extensions);
            }
            else
            {
               switch (mergeType)
               {
               case AUTO:
                  if (!current.getContent().isEmpty())
                  {
                     if (hTarget.getState() == ContentState.New)
                     {
                        targetChanged |= resourceUtils.transferFromTextFlowTarget(current, hTarget);
                        targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(current.getExtensions(true), hTarget, extensions);
                     }
                     else
                     {
                        String localContent = current.getContent();
                        boolean matchHistory = textFlowTargetHistoryDAO.findContentInHistory(hTarget, localContent);
                        if (!matchHistory)
                        {
                           targetChanged |= resourceUtils.transferFromTextFlowTarget(current, hTarget);
                           targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(current.getExtensions(true), hTarget, extensions);
                        }
                     }
                  }
                  break;

               case IMPORT:
                  removedTargets.remove(hTarget);
                  targetChanged |= resourceUtils.transferFromTextFlowTarget(current, hTarget);
                  targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(current.getExtensions(true), hTarget, extensions);
                  break;

               default:
                  return Response.status(Status.INTERNAL_SERVER_ERROR).entity("unhandled merge type " + mergeType).build();
               }
            }

            // update translation information if applicable
            if (targetChanged)
            {
               changed = true;
               if (current.getTranslator() != null)
               {
                  String email = current.getTranslator().getEmail();
                  HPerson hPerson = personDAO.findByEmail(email);
                  if (hPerson == null)
                  {
                     hPerson = new HPerson();
                     hPerson.setEmail(email);
                     hPerson.setName(current.getTranslator().getName());
                     newPeople.add(hPerson);
                  }
                  hTarget.setLastModifiedBy(hPerson);
               }
               else
               {
                  hTarget.setLastModifiedBy(null);
               }
               textFlowTargetDAO.makePersistent(hTarget);
            }
            current = null;
         }
      }
      if (changed || !removedTargets.isEmpty())
      {
         for (HPerson person : newPeople)
         {
            personDAO.makePersistent(person);
         }
         personDAO.flush();

         for (HTextFlowTarget target : removedTargets)
         {
            target.clear();
         }
         textFlowTargetDAO.flush();

         documentDAO.flush();

         // TODO create valid etag
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
      }
      log.debug("successful put translation");
      // TODO lastChanged
      if (unknownResIds.isEmpty())
         return Response.ok().tag(etag).build();
      else
         return Response.ok("warning: unknown resIds: " + unknownResIds).tag(etag).build();
   }

   private HProjectIteration retrieveIteration()
   {
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      if (hProjectIteration != null)
      {
         return hProjectIteration;
      }

      throw new NoSuchEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
   }

   private void validateExtensions(String... extensions)
   {
      Set<String> validExtensions = Sets.newHashSet(extensions);
      Set<String> invalidExtensions = null;
      for (String ext : extensions)
      {
         if (!validExtensions.contains(ext))
         {
            if (invalidExtensions == null)
            {
               invalidExtensions = new HashSet<String>();
            }
            invalidExtensions.add(ext);
         }
      }

      if (invalidExtensions != null)
      {
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("Unsupported Extensions within this context: " + StringUtils.join(invalidExtensions, ",")).build());
      }
   }
   

   public void copyClosestEquivalentTranslation(Long docId, String name, String projectSlug, String iterationSlug)
   {
      if (zanataInit.getEnableCopyTrans())
      {
         Events.instance().raiseTransactionSuccessEvent(EVENT_COPY_TRANS, docId, projectSlug, iterationSlug);
      }
   }

}
