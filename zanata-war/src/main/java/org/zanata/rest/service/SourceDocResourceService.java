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

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.Namespaces;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.service.CopyTransService;
import org.zanata.service.DocumentService;
import org.zanata.service.LocaleService;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("sourceDocResourceService")
@Path(SourceDocResourceService.SERVICE_PATH)
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Transactional
public class SourceDocResourceService implements SourceDocResource
{
   public static final String SERVICE_PATH = ProjectIterationService.SERVICE_PATH + "/r";

   @Logger
   private Log log;

   @Context
   private Request request;

   @Context
   private UriInfo uri;

   /** Project Identifier. */
   @PathParam("projectSlug")
   private String projectSlug;

   /** Project Iteration identifier. */
   @PathParam("iterationSlug")
   private String iterationSlug;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private DocumentDAO documentDAO;

   @In
   private LocaleService localeServiceImpl;

   @In
   private CopyTransService copyTransServiceImpl;

   @In
   private DocumentService documentServiceImpl;

   @In
   private ApplicationConfiguration applicationConfiguration;

   @In
   private ResourceUtils resourceUtils;

   @In
   private ETagUtils eTagUtils;


   /**
    * Returns header information for a Project's iteration translations.
    *
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Response containing an "Etag" header for the requested project iteration translations.<br>
    * NOT FOUND(404) - If a project iteration could not be found for the given parameters.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @HEAD
   public Response head()
   {
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);
      EntityTag etag = projectIterationDAO.getResourcesETag(hProjectIteration);
      Response.ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }
      return Response.ok().tag(etag).build();
   }

   /**
    * Retrieve the List of Documents (Resources) belonging to a Project iteration.
    *
    * @param extensions The document extensions to fetch along with the documents (e.g. "gettext", "comment"). This parameter
    * allows multiple values e.g. "ext=gettext&ext=comment".
    * @return  The following response status codes will be returned from this operation:<br>
    * OK(200) - Response with a list of documents wrapped around a "resources" tag. <br>
    * NOT FOUND(404) - If a Project iteration could not be found with the given parameters.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Wrapped(element = "resources", namespace = Namespaces.ZANATA_API)
   @TypeHint(ResourceMeta.class)
   public Response get(@QueryParam("ext") Set<String> extensions)
   {
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);

      EntityTag etag = projectIterationDAO.getResourcesETag(hProjectIteration);

      Response.ResponseBuilder response = request.evaluatePreconditions(etag);
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


   /**
    * Creates a new Document.
    *
    * @param resource The document information.
    * @param extensions The document extensions to save with the new document (e.g. "gettext", "comment"). This parameter
    * allows multiple values e.g. "ext=gettext&ext=comment".
    * @param copytrans Boolean value that indicates whether reasonably close translations from other projects should be
    * found to initially populate this document's translations.
    * @return The following response status codes will be returned from this operation:<br>
    * CREATED (201) - If the document was successfully created.<br>
    * CONFLICT(409) - If another document already exists with the same name, on the same project iteration.<br>
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @POST
   @Restrict("#{s:hasPermission(sourceDocResourceService.securedIteration, 'import-template')}")
   public Response post(Resource resource, @QueryParam("ext") Set<String> extensions, @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans)
   {
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);

      resourceUtils.validateExtensions(extensions); //gettext, comment

      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, resource.getName());

      // already existing non-obsolete document.
      if (document != null)
      {
         if (!document.isObsolete())
         {
            // updates must happen through PUT on the actual resource
            return Response.status(Response.Status.CONFLICT).entity("A document with name " + resource.getName() + " already exists.").build();
         }
      }

      // TODO No need for docId param since it's resource.getName()
      document =
            this.documentServiceImpl.saveDocument(
                  this.projectSlug, this.iterationSlug, resource, extensions, copytrans);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, document.getDocId(), extensions);

      return Response.created(URI.create("r/" + resourceUtils.encodeDocId(document.getDocId()))).tag(etag).build();
   }

   /**
    * Retrieves information for a Document.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param extensions The document extensions to fetch along with the document (e.g. "gettext", "comment"). This parameter
    * allows multiple values e.g. "ext=gettext&ext=comment".
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Response with the document's information.<br>
    * NOT FOUND(404) - If a document could not be found with the given parameters.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE)
   @TypeHint(Resource.class)
   // /r/{id}
   public Response getResource(@PathParam("id") String idNoSlash, @QueryParam("ext") Set<String> extensions)
   {
      log.debug("start get resource");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);

      resourceUtils.validateExtensions(extensions);

      final Set<String> extSet = new HashSet<String>(extensions);
      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extSet);

      Response.ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument doc = documentDAO.getByDocIdAndIteration(hProjectIteration, id);

      if (doc == null || doc.isObsolete())
      {
         return Response.status(Response.Status.NOT_FOUND).entity("document not found").build();
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

   /**
    * Creates or modifies a Document.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param resource The document information.
    * @param extensions The document extensions to save with the document (e.g. "gettext", "comment"). This parameter
    * allows multiple values e.g. "ext=gettext&ext=comment".
    * @param copytrans Boolean value that indicates whether reasonably close translations from other projects should be
    * found to initially populate this document's translations.
    * @return The following response status codes will be returned from this operation:<br>
    * CREATED(201) - If a new document was successfully created.<br>
    * OK(200) - If an already existing document was modified.<br>
    * NOT FOUND(404) - If a project or project iteration could not be found with the given parameters.<br>
    * FORBIDDEN(403) - If the user is not allowed to modify the project, project iteration or document. This might be
    * due to the project or iteration being in Read-Only mode.<br>
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE)
   @Restrict("#{s:hasPermission(sourceDocResourceService.securedIteration, 'import-template')}")
   // /r/{id}
   public Response putResource(@PathParam("id") String idNoSlash, Resource resource, @QueryParam("ext") Set<String> extensions, @QueryParam("copyTrans") @DefaultValue("true") boolean copytrans)
   {
      log.debug("start put resource");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      Response.ResponseBuilder response;
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);

      resourceUtils.validateExtensions(extensions);

      HDocument document = this.documentDAO.getByDocIdAndIteration(hProjectIteration, id);
      if( document == null || document.isObsolete() )
      {
         response = Response.created(uri.getAbsolutePath());
      }
      else
      {
         response = Response.ok();
      }

      resource.setName( id );
      document = this.documentServiceImpl.saveDocument(projectSlug, iterationSlug, resource, extensions, copytrans);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, document.getDocId(), extensions);

      log.debug("put resource successfully");
      return response.tag(etag).build();
   }

   /**
    * Delete a Document. The system keeps the history of this document however.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - If The document was successfully deleted.<br>
    * NOT FOUND(404) - If a project or project iteration could not be found with the given parameters.<br>
    * FORBIDDEN(403) - If the user is not allowed to modify the project, project iteration or document. This might be
    * due to the project or iteration being in Read-Only mode.<br>
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE)
   @Restrict("#{s:hasPermission(sourceDocResourceService.securedIteration, 'import-template')}")
   // /r/{id}
   public Response deleteResource(@PathParam("id") String idNoSlash)
   {
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, new HashSet<String>());

      Response.ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, id);
      document.setObsolete(true);
      documentDAO.flush();
      return Response.ok().build();
   }

   /**
    * Retrieves meta-data information for a Document.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param extensions The document extensions to retrieve with the document's meta-data (e.g. "gettext", "comment").
    * This parameter allows multiple values e.g. "ext=gettext&ext=comment".
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - If the Document's meta-data was found. The data will be contained in the response.<br>
    * NOT FOUND(404) - If a project, project iteration or document could not be found with the given parameters.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   @TypeHint(ResourceMeta.class)
   // /r/{id}/meta
   public Response getResourceMeta(@PathParam("id") String idNoSlash, @QueryParam("ext") Set<String> extensions)
   {
      log.debug("start to get resource meta");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(false);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      Response.ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HDocument doc = documentDAO.getByDocIdAndIteration(hProjectIteration, id);

      if (doc == null)
      {
         return Response.status(Response.Status.NOT_FOUND).entity("document not found").build();
      }

      ResourceMeta entity = new ResourceMeta(doc.getDocId());
      resourceUtils.transferToAbstractResourceMeta(doc, entity);

      // transfer extensions
      resourceUtils.transferToResourceExtensions(doc, entity.getExtensions(true), extensions);

      log.debug("successfuly get resource meta: {0}" , entity);
      return Response.ok().entity(entity).tag(etag).build();
   }

   /**
    * Modifies an existing document's meta-data.
    *
    * @param idNoSlash The document identifier. Some document ids could have forward slashes ('/') in them which would
    * cause conflicts with the browser's own url interpreter. For this reason, the supplied id must have all its '/'
    * characters replaced with commas (',').
    * @param messageBody The document's meta-data.
    * @param extensions The document extensions to save with the document (e.g. "gettext", "comment"). This parameter
    * allows multiple values e.g. "ext=gettext&ext=comment".
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - If the Document's meta-data was successfully modified.<br>
    * NOT FOUND(404) - If a document was not found using the given parameters.<br>
    * UNAUTHORIZED(401) - If the user does not have the proper permissions to perform this operation.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   @Restrict("#{s:hasPermission(sourceDocResourceService.securedIteration, 'import-template')}")
   // /r/{id}/meta
   public Response putResourceMeta(@PathParam("id") String idNoSlash, ResourceMeta messageBody, @QueryParam("ext") Set<String> extensions)
   {
      log.debug("start to put resource meta");
      String id = URIHelper.convertFromDocumentURIId(idNoSlash);
      HProjectIteration hProjectIteration = retrieveAndCheckIteration(true);

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);

      Response.ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      log.debug("pass evaluation");
      log.debug("put resource meta: {0}", messageBody);

      HDocument document = documentDAO.getByDocIdAndIteration(hProjectIteration, id);
      if (document == null)
      {
         return Response.status(Response.Status.NOT_FOUND).build();
      }
      if (document.isObsolete())
      {
         return Response.status(Response.Status.NOT_FOUND).build();
      }
      HLocale hLocale = validateTargetLocale(messageBody.getLang(), projectSlug, iterationSlug);
      boolean changed = resourceUtils.transferFromResourceMetadata(messageBody, document, extensions, hLocale, document.getRevision() + 1);

      if (changed)
      {
         documentDAO.flush();
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
      }

      log.debug("put resource meta successfully");
      return Response.ok().tag(etag).lastModified(document.getLastChanged()).build();

   }

   private HProjectIteration retrieveAndCheckIteration(boolean writeOperation)
   {
      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HProject hProject = hProjectIteration == null ? null : hProjectIteration.getProject();

      if (hProjectIteration == null)
      {
         throw new NoSuchEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
      }
      else if (hProjectIteration.getStatus().equals(EntityStatus.OBSOLETE) || hProject.getStatus().equals(EntityStatus.OBSOLETE))
      {
         throw new NoSuchEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' not found.");
      }
      else if (writeOperation)
      {
         if (hProjectIteration.getStatus().equals(EntityStatus.READONLY) || hProject.getStatus().equals(EntityStatus.READONLY))
         {
            throw new ReadOnlyEntityException("Project Iteration '" + projectSlug + ":" + iterationSlug + "' is read-only.");
         }
         else
         {
            return hProjectIteration;
         }
      }
      else
      {
         return hProjectIteration;
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
         throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build());
      }
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
         throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).entity(e.getMessage()).build());
      }
   }

   public void copyClosestEquivalentTranslation(HDocument document)
   {
      if (applicationConfiguration.getEnableCopyTrans())
      {
         copyTransServiceImpl.copyTransForDocument(document);
      }
   }

   public HProjectIteration getSecuredIteration()
   {
      return retrieveAndCheckIteration(false);
   }

}
