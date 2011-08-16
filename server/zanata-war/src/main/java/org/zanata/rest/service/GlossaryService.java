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
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.HTermComment;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.service.LocaleService;

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

   private Log log = Logging.getLog(GlossaryService.class);

   @In
   private AccountDAO accountDAO;

   @In
   private GlossaryDAO glossaryDAO;

   @In
   private Identity identity;

   @In
   private ETagUtils eTagUtils;

   @In
   private LocaleService localeService;

   public GlossaryService()
   {
   }

   public GlossaryService(GlossaryDAO glossaryDAO, AccountDAO accountDAO, Identity identity, ETagUtils eTagUtils, LocaleService localeService)
   {
      this.glossaryDAO = glossaryDAO;
      this.accountDAO = accountDAO;
      this.identity = identity;
      this.eTagUtils = eTagUtils;
      this.localeService = localeService;
   }

   @Override
   @GET
   @Path(SERVICE_PATH)
   @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
   public Response getEntries()
   {
      EntityTag etag = eTagUtils.generateTagForGlossaryTerm(LocaleId.EN_US);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntries();

      Glossary glossary = new Glossary();
      transferEntriesResource(hGlosssaryEntries, glossary);

      return Response.ok(glossary).tag(etag).build();
   }

   @Override
   @GET
   @Path(SERVICE_PATH + "/{locale}")
   @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
   public Response get(@PathParam("locale") LocaleId locale)
   {
      EntityTag etag = eTagUtils.generateTagForGlossaryTerm(locale);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntriesByLocaleId(locale);
      Glossary glossary = new Glossary();

      transferEntriesLocaleResource(hGlosssaryEntries, glossary, locale);

      return Response.ok(glossary).tag(etag).build();
   }

   @Override
   @PUT
   @Path(SERVICE_PATH)
   @Consumes({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
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

      List<Long> hGlossaryEntryIds = new ArrayList<Long>();
      for (GlossaryEntry glossaryEntry : glossary.getGlossaryEntries())
      {
         hGlossaryEntry = new HGlossaryEntry();
         transferGlossaryEntry(glossaryEntry, hGlossaryEntry);
         hGlossaryEntry = glossaryDAO.makePersistent(hGlossaryEntry);
         hGlossaryEntryIds.add(hGlossaryEntry.getId().longValue());
      }
      glossaryDAO.flush();

      etag = eTagUtils.generateTagForGlossary(hGlossaryEntryIds);
      return response.tag(etag).build();
   }

   public void transferGlossaryEntry(GlossaryEntry from, HGlossaryEntry to)
   {
      for (GlossaryTerm glossaryTerm : from.getGlossaryTerms())
      {
         HGlossaryTerm hGlossaryTerm = new HGlossaryTerm(glossaryTerm.getContent());
         hGlossaryTerm.setSourceRef(glossaryTerm.getSourcereference());

         for (String comment : glossaryTerm.getComments())
         {
            hGlossaryTerm.getComments().add(new HTermComment(comment));
         }
         localeService.save(glossaryTerm.getLocale());
         HLocale targetLocale = localeService.getByLocaleId(glossaryTerm.getLocale());
         hGlossaryTerm.setLocale(targetLocale);

         // check if term equals to sourceLang
         if (targetLocale.getLocaleId().equals(from.getSrcLang()))
         {
            to.setSrcTerm(hGlossaryTerm);
         }
         to.getGlossaryTerms().put(targetLocale, hGlossaryTerm);
      }
   }

   public void transferEntriesResource(List<HGlossaryEntry> hGlosssaryEntries, Glossary glossary)
   {
      for (HGlossaryEntry hGlossaryEntry : hGlosssaryEntries)
      {
         GlossaryEntry glossaryEntry = new GlossaryEntry();
         glossaryEntry.setSrcLang(hGlossaryEntry.getSrcTerm().getLocale().getLocaleId());

         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            GlossaryTerm glossaryTerm = new GlossaryTerm();
            glossaryTerm.setContent(hGlossaryTerm.getContent());
            glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());
            glossaryTerm.setSourcereference(hGlossaryTerm.getSourceRef());

            for (HTermComment hTermComment : hGlossaryTerm.getComments())
            {
               glossaryTerm.getComments().add(hTermComment.getComment());
            }
            glossaryEntry.getGlossaryTerms().add(glossaryTerm);
         }
         glossary.getGlossaryEntries().add(glossaryEntry);
      }
   }

   public static void transferEntriesLocaleResource(List<HGlossaryEntry> hGlosssaryEntries, Glossary glossary, LocaleId locale)
   {
      for (HGlossaryEntry hGlossaryEntry : hGlosssaryEntries)
      {
         GlossaryEntry glossaryEntry = new GlossaryEntry();
         glossaryEntry.setSrcLang(hGlossaryEntry.getSrcTerm().getLocale().getLocaleId());

         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            if (hGlossaryTerm.getLocale().getLocaleId().equals(locale))
            {
               GlossaryTerm glossaryTerm = new GlossaryTerm();
               glossaryTerm.setContent(hGlossaryTerm.getContent());
               glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());
               glossaryTerm.setSourcereference(hGlossaryTerm.getSourceRef());

               for (HTermComment hTermComment : hGlossaryTerm.getComments())
               {
                  glossaryTerm.getComments().add(hTermComment.getComment());
               }
               glossaryEntry.getGlossaryTerms().add(glossaryTerm);
            }
         }
         glossary.getGlossaryEntries().add(glossaryEntry);
      }
   }
}
