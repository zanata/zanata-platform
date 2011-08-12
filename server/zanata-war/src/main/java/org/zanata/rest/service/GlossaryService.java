package org.zanata.rest.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.GlossaryEntryDAO;
import org.zanata.dao.GlossaryTermDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

@Name("glossaryService")
@Path(GlossaryService.SERVICE_PATH)
@Transactional
public class GlossaryService implements GlossaryResource
{
   @Context
   private UriInfo uri;

   @HeaderParam("Content-Type")
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   Log log = Logging.getLog(GlossaryService.class);

   @In
   AccountDAO accountDAO;

   @In
   GlossaryEntryDAO glossaryEntryDAO;

   @In
   GlossaryTermDAO glossaryTermDAO;

   @In
   Identity identity;

   @In
   ETagUtils eTagUtils;

   public GlossaryService()
   {
   }

   public GlossaryService(GlossaryEntryDAO glossaryEntryDAO, AccountDAO accountDAO, Identity identity, ETagUtils eTagUtils)
   {
      this.glossaryEntryDAO = glossaryEntryDAO;
      this.accountDAO = accountDAO;
      this.identity = identity;
      this.eTagUtils = eTagUtils;
   }

   @Override
   @GET
   @Path(SERVICE_PATH + "/{locale}")
   @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response get(@PathParam("locale") LocaleId locale)
   {
      EntityTag etag = eTagUtils.generateTagForGlossaryTerm(locale);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryTerm> hGlosssaryTerm = glossaryTermDAO.findByLocaleId(locale);
      List<GlossaryTerm> glossaryTerm = new ArrayList<GlossaryTerm>();

      transferGlossaryTermResource(hGlosssaryTerm, glossaryTerm);

      return Response.ok(glossaryTerm).tag(etag).build();
   }

   @Override
   @PUT
   @Consumes({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response put(InputStream messageBody)
   {
      ResponseBuilder response;
      EntityTag etag;

      // must be a create operation
      response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      HGlossaryEntry hGlossaryEntry = new HGlossaryEntry();
      // pre-emptive entity permission check
      identity.checkPermission(hGlossaryEntry, "insert");

      response = Response.created(uri.getAbsolutePath());

      Glossary glossary = RestUtils.unmarshall(Glossary.class, messageBody, requestContentType, headers.getRequestHeaders());

      List<Integer> hGlossaryEntryIds = new ArrayList<Integer>();
      for (GlossaryEntry glossaryEntry : glossary.getGlossaryEntries())
      {
         hGlossaryEntry = new HGlossaryEntry();
         transferGlossaryEntry(glossaryEntry, hGlossaryEntry);
         hGlossaryEntry = glossaryEntryDAO.makePersistent(hGlossaryEntry);
         hGlossaryEntryIds.add(hGlossaryEntry.getId().intValue());
      }

      glossaryEntryDAO.flush();

      etag = eTagUtils.generateTagForGlossary(hGlossaryEntryIds);
      return response.tag(etag).build();
   }

   public static void transferGlossaryEntry(GlossaryEntry from, HGlossaryEntry to)
   {
      for (GlossaryTerm glossaryTerm : from.getGlossaryTerms())
      {
         HGlossaryTerm hGlossaryTerm = new HGlossaryTerm(glossaryTerm.getContent());
         for (String comment : glossaryTerm.getComments())
         {
            hGlossaryTerm.getComments().add(new HTermComment(comment));
         }
         to.getGlossaryTerms().put(new HLocale(glossaryTerm.getLocale()), hGlossaryTerm);
      }
   }

   public static void transferGlossaryTermResource(List<HGlossaryTerm> hGlosssaryTerms, List<GlossaryTerm> glossaryTerms)
   {
      for (HGlossaryTerm hGlossaryTerm : hGlosssaryTerms)
      {
         GlossaryTerm glossaryTerm = new GlossaryTerm();
         glossaryTerm.setContent(hGlossaryTerm.getContent());
         glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());
         glossaryTerm.setSourcereference(hGlossaryTerm.getSourceRef());

         for (HTermComment hTermComment : hGlossaryTerm.getComments())
         {
            glossaryTerm.getComments().add(hTermComment.getComment());
         }
         glossaryTerms.add(glossaryTerm);
      }
   }
}
