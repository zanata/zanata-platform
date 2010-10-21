package net.openl10n.flies.rest.service;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.common.Namespaces;
import net.openl10n.flies.dao.DocumentDAO;
import net.openl10n.flies.dao.PersonDAO;
import net.openl10n.flies.dao.ProjectIterationDAO;
import net.openl10n.flies.dao.TextFlowTargetDAO;
import net.openl10n.flies.exception.FliesServiceException;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HProjectIteration;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.rest.NoSuchEntityException;
import net.openl10n.flies.rest.dto.extensions.gettext.PoHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PotEntryHeader;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;
import net.openl10n.flies.service.LocaleService;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Admin;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

import com.google.common.collect.Sets;

@Name("translationResourcesService")
@Path(TranslationResourcesService.SERVICE_PATH)
@Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class TranslationResourcesService
{

   // security actions
   private static final String ACTION_IMPORT_TEMPLATE = "import-template";
   private static final String ACTION_IMPORT_TRANSLATION = "import-translation";

   public static final String RESOURCE_SLUG_TEMPLATE = "/{id:[a-zA-Z0-9]+([a-zA-Z0-9_\\-,{.}]*[a-zA-Z0-9]+)?}";

   public static final String SERVICE_PATH = ProjectIterationService.SERVICE_PATH + "/r";

   @PathParam("projectSlug")
   private String projectSlug;

   @PathParam("iterationSlug")
   private String iterationSlug;

   @QueryParam("ext")
   @DefaultValue("")
   private Set<String> extensions;


   @HeaderParam("Content-Type")
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   @Context
   private UriInfo uri;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private DocumentDAO documentDAO;

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
   @GET
   @Wrapped(element = "resources", namespace = Namespaces.FLIES)
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
         if (!doc.isObsolete())
         {
            ResourceMeta resource = new ResourceMeta();
            resourceUtils.transferToAbstractResourceMeta(doc, resource);
            resources.add(resource);
         }
      }

      return Response.ok(new GenericEntity<List<ResourceMeta>>(resources, new GenericType<List<ResourceMeta>>()
      {
      }.getGenericType())).tag(etag).build();
   }


   @POST
   @Admin
   public Response post(InputStream messageBody)
   {

      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TEMPLATE);

      validateExtensions(PoHeader.ID, PotEntryHeader.ID);

      Resource entity = RestUtils.unmarshall(Resource.class, messageBody, requestContentType, headers.getRequestHeaders());

      HDocument document = documentDAO.getByDocId(hProjectIteration, entity.getName());
      HLocale hLocale = validateLocale(entity.getLang());
      if (document != null)
      {
         if (!document.isObsolete())
         {
            // updates happens through PUT on the actual resource
            return Response.status(Status.CONFLICT).entity("A document with name " + entity.getName() + " already exists.").build();
         }
         // a deleted document is being created again
         document.setObsolete(false);
      }
      else
      {
         document = new HDocument(entity.getName(), entity.getContentType(), hLocale);
         document.setProjectIteration(hProjectIteration);
      }

      resourceUtils.transferFromResource(entity, document, extensions, hLocale);

      document = documentDAO.makePersistent(document);
      documentDAO.flush();

      EntityTag etag = eTagUtils.generateETagForDocument(hProjectIteration, document.getDocId(), extensions);

      return Response.created(URI.create("r/" + resourceUtils.encodeDocId(document.getDocId()))).tag(etag).build();
   }

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   public Response getResource(@PathParam("id") String id)
   {
      log.debug("start get resource");
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

   private HLocale validateLocale(LocaleId locale)
   {
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.getSupportedLanguageByLocale(locale);
         return hLocale;
      }
      catch (FliesServiceException e)
      {
         // TODO perhaps we should use status code 403 here?
         throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
      }

   }

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   @Admin
   public Response putResource(@PathParam("id") String id, InputStream messageBody)
   {
      log.debug("start put resource");

      ResponseBuilder response;
      EntityTag etag = null;
      boolean changed = false;
      HProjectIteration hProjectIteration = retrieveIteration();

      identity.checkPermission(hProjectIteration, ACTION_IMPORT_TEMPLATE);

      validateExtensions();

      Resource entity = RestUtils.unmarshall(Resource.class, messageBody, requestContentType, headers.getRequestHeaders());
      log.debug("resource details: {0}", entity);

      HDocument document = documentDAO.getByDocId(hProjectIteration, id);
      LocaleId locale = entity.getLang();
      HLocale hLocale = validateLocale(locale);
      if (document == null)
      { // must be a create operation
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }
         changed = true;
         document = new HDocument(entity.getName(), entity.getContentType(), hLocale);
         document.setProjectIteration(hProjectIteration);
         response = Response.created(uri.getAbsolutePath());

      }
      else if (document.isObsolete())
      { // must also be a create operation
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }
         changed = true;
         document.setObsolete(false);
         response = Response.created(uri.getAbsolutePath());
      }
      else
      { // must be an update operation
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
         response = request.evaluatePreconditions(etag);
         if (response != null)
         {
            return response.build();
         }

         response = Response.ok();
      }

      changed |= resourceUtils.transferFromResource(entity, document, extensions, hLocale);


      if (changed)
      {
         document = documentDAO.makePersistent(document);
         documentDAO.flush();
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
      }

      log.debug("put resource successfully");
      return response.tag(etag).build();

   }

   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE)
   // /r/{id}
   @Admin
   public Response deleteResource(@PathParam("id") String id)
   {
      HProjectIteration hProjectIteration = retrieveIteration();

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

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   public Response getResourceMeta(@PathParam("id") String id)
   {
      log.debug("start to get resource meta");
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

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/meta")
   // /r/{id}/meta
   @Admin
   public Response putResourceMeta(@PathParam("id") String id, InputStream messageBody)
   {
      log.debug("start to put resource meta");
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
      // FIXME what if document is null? (ie putting new document)
      if (document.isObsolete())
      {
         return Response.status(Status.NOT_FOUND).build();
      }
      HLocale hLocale = validateLocale(entity.getLang());
      boolean changed = resourceUtils.transferFromResourceMetadata(entity, document, extensions, hLocale);


      if (changed)
      {
         documentDAO.flush();
         etag = eTagUtils.generateETagForDocument(hProjectIteration, id, extensions);
      }

      log.debug("put resource meta successfully");
      return Response.ok().tag(etag).lastModified(document.getLastChanged()).build();

   }

   @GET
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   public Response getTranslations(@PathParam("id") String id, @PathParam("locale") LocaleId locale)
   {
      log.debug("start to get translation");
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

      List<HTextFlowTarget> hTargets = textFlowTargetDAO.findAllTranslations(document, locale);
      HLocale hLocale;
      try
      {
         hLocale = localeServiceImpl.getSupportedLanguageByLocale(locale);
      }
      catch (FliesServiceException e)
      {
         return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
      }
      TranslationsResource translationResource = new TranslationsResource();
      resourceUtils.transferToTranslationsResourceExtensions(document, translationResource.getExtensions(true), extensions, hLocale);

      if (hTargets.isEmpty() && translationResource.getExtensions(true).isEmpty())
      {
         return Response.status(Status.NOT_FOUND).build();
      }

      for (HTextFlowTarget hTarget : hTargets)
      {
         TextFlowTarget target = new TextFlowTarget(hTarget.getTextFlow().getResId());
         resourceUtils.transferToTextFlowTarget(hTarget, target);
         resourceUtils.transferToTextFlowTargetExtensions(hTarget, target.getExtensions(true), extensions);
         translationResource.getTextFlowTargets().add(target);
      }

      // TODO lastChanged
      return Response.ok().entity(translationResource).tag(etag).build();

   }

   @DELETE
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   @Admin
   public Response deleteTranslations(@PathParam("id") String id, @PathParam("locale") LocaleId locale)
   {

      HProjectIteration hProjectIteration = retrieveIteration();

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

   @PUT
   @Path(RESOURCE_SLUG_TEMPLATE + "/translations/{locale}")
   // /r/{id}/translations/{locale}
   @Admin
   public Response putTranslations(@PathParam("id") String id, @PathParam("locale") LocaleId locale, InputStream messageBody)
   {
      log.debug("start put translations");

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

      HLocale hLocale = validateLocale(locale);
      // handle extensions
      changed |= resourceUtils.transferFromTranslationsResourceExtensions(entity.getExtensions(true), document, extensions, hLocale);

      List<HPerson> newPeople = new ArrayList<HPerson>();
      List<HTextFlowTarget> newTargets = new ArrayList<HTextFlowTarget>();
      List<HTextFlowTarget> changedTargets = new ArrayList<HTextFlowTarget>();
      List<HTextFlowTarget> removedTargets = new ArrayList<HTextFlowTarget>();

      Iterator<TextFlowTarget> iter = entity.getTextFlowTargets().iterator();
      TextFlowTarget current = null;
      for (HTextFlow textFlow : document.getTextFlows())
      {
         if (current == null)
         {
            if (iter.hasNext())
            {
               current = iter.next();
            }
            else
            {
               HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
               if (hTarget != null)
               {
                  removedTargets.add(hTarget);
               }
               continue;
            }
         }

         if (textFlow.getResId().equals(current.getResId()))
         {
            // transfer

            HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
            boolean targetChanged = false;
            if (hTarget == null)
            {
               targetChanged = true;
               log.debug("locale:" + locale);
               hTarget = new HTextFlowTarget(textFlow, hLocale);
               textFlow.getTargets().put(hLocale, hTarget);
               newTargets.add(hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTarget(current, hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(current.getExtensions(true), hTarget, extensions);
            }
            else
            {
               targetChanged |= resourceUtils.transferFromTextFlowTarget(current, hTarget);
               targetChanged |= resourceUtils.transferFromTextFlowTargetExtensions(current.getExtensions(true), hTarget, extensions);
               if (targetChanged)
               {
                  changedTargets.add(hTarget);
               }
            }

            // update translation information if applicable
            if (targetChanged && current.getTranslator() != null)
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

            current = null;
         }
         else
         {
            HTextFlowTarget hTarget = textFlow.getTargets().get(hLocale);
            if (hTarget != null)
            {
               removedTargets.add(hTarget);
            }
         }

      }

      if (iter.hasNext())
      {
         return Response.status(Status.BAD_REQUEST).entity("Unexpected target: " + iter.next().getResId()).build();
      }
      else if (changed || !newTargets.isEmpty() || !changedTargets.isEmpty() || !removedTargets.isEmpty())
      {

         for (HPerson person : newPeople)
         {
            personDAO.makePersistent(person);
         }
         personDAO.flush();

         for (HTextFlowTarget target : newTargets)
         {
            textFlowTargetDAO.makePersistent(target);
         }
         textFlowTargetDAO.flush();

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
      return Response.ok().tag(etag).build();
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

}
