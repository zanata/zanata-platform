package org.zanata.rest.service;

import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
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
   private LocaleService localeServiceImpl;

   private final static String GLOSSARY_ACTION_INSERT = "glossary-insert";
   private final static String GLOSSARY_ACTION_DELETE = "glossary-delete";
   private final static String GLOSSARY_ACTION_UPDATE = "glossary-update";

   public GlossaryService()
   {
   }

   public GlossaryService(GlossaryDAO glossaryDAO, AccountDAO accountDAO, Identity identity, LocaleService localeService)
   {
      this.glossaryDAO = glossaryDAO;
      this.accountDAO = accountDAO;
      this.identity = identity;
      this.localeServiceImpl = localeService;
   }

   @Override
   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response getEntries()
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntries();

      Glossary glossary = new Glossary();
      transferEntriesResource(hGlosssaryEntries, glossary);

      return Response.ok(glossary).build();
   }

   @Override
   @GET
   @Path("/{locale}")
   @Produces( { MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response get(@PathParam("locale") LocaleId locale)
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      List<HGlossaryEntry> hGlosssaryEntries = glossaryDAO.getEntriesByLocaleId(locale);
      Glossary glossary = new Glossary();

      transferEntriesLocaleResource(hGlosssaryEntries, glossary, locale);

      return Response.ok(glossary).build();
   }

   @Override
   @PUT
   @Consumes( { MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response put(InputStream messageBody)
   {
      ResponseBuilder response;

      // must be a create operation
      response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      response = Response.created(uri.getAbsolutePath());
      Glossary glossary = RestUtils.unmarshall(Glossary.class, messageBody, requestContentType, headers.getRequestHeaders());

      identity.checkPermission("", GLOSSARY_ACTION_INSERT);

      for (GlossaryEntry glossaryEntry : glossary.getGlossaryEntries())
      {
         transferGlossaryEntry(glossaryEntry);
      }
      glossaryDAO.flush();

      return response.build();
   }

   /**
    * Delete all glossary term with specified locale. GlossaryEntry will be
    * deleted if termList is empty
    */
   @Override
   @DELETE
   @Path("/{locale}")
   public Response deleteGlossary(@PathParam("locale") LocaleId targetLocale)
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      identity.checkPermission("", GLOSSARY_ACTION_DELETE);

      List<HGlossaryEntry> hGlossaryEntries = glossaryDAO.getEntries();

      for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries)
      {
         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            if (hGlossaryTerm.getLocale().getLocaleId().equals(targetLocale))
            {
               hGlossaryEntry.getGlossaryTerms().remove(hGlossaryTerm.getLocale());
            }
         }

         if (hGlossaryEntry.getGlossaryTerms().isEmpty())
         {
            glossaryDAO.makeTransient(hGlossaryEntry);
         }
         else
         {
            glossaryDAO.makePersistent(hGlossaryEntry);
         }

         glossaryDAO.flush();

      }

      return Response.ok().build();
   }

   @Override
   @DELETE
   public Response deleteGlossaries()
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      List<HGlossaryEntry> hGlossaryEntries = glossaryDAO.getEntries();

      identity.checkPermission("", GLOSSARY_ACTION_DELETE);
      for (HGlossaryEntry hGlossaryEntry : hGlossaryEntries)
      {
         glossaryDAO.makeTransient(hGlossaryEntry);
      }
      glossaryDAO.flush();

      return Response.ok().build();
   }

   private HGlossaryTerm getOrCreateGlossaryTerm(HGlossaryEntry hGlossaryEntry, HLocale termHLocale, String content)
   {
      HGlossaryTerm hGlossaryTerm = hGlossaryEntry.getGlossaryTerms().get(termHLocale);

      if (hGlossaryTerm == null)
      {
         hGlossaryTerm = new HGlossaryTerm(content);
         hGlossaryTerm.setLocale(termHLocale);
         hGlossaryTerm.setGlossaryEntry(hGlossaryEntry);
         hGlossaryEntry.getGlossaryTerms().put(termHLocale, hGlossaryTerm);
      }

      return hGlossaryTerm;
   }

   private HGlossaryEntry getOrCreateGlossaryEntry(LocaleId srcLocale, String srcContent)
   {
      HGlossaryEntry hGlossaryEntry = glossaryDAO.getEntryBySrcContentLocale(srcLocale, srcContent);

      if (hGlossaryEntry == null)
      {
         hGlossaryEntry = new HGlossaryEntry();
         HLocale srcHLocale = localeServiceImpl.getByLocaleId(srcLocale);
         hGlossaryEntry.setSrcLocale(srcHLocale);
      }
      return hGlossaryEntry;
   }

   public String getSrcGlossaryTerm(GlossaryEntry entry)
   {
      for (GlossaryTerm term : entry.getGlossaryTerms())
      {
         if (term.getLocale().equals(entry.getSrcLang()))
         {
            return term.getContent();
         }
      }
      return null;
   }

   public void transferGlossaryEntry(GlossaryEntry from)
   {
      HGlossaryEntry to = getOrCreateGlossaryEntry(from.getSrcLang(), getSrcGlossaryTerm(from));
            
      to.setSourceRef(from.getSourcereference());

      for (GlossaryTerm glossaryTerm : from.getGlossaryTerms())
      {
         HLocale termHLocale = localeServiceImpl.validateSourceLocale(glossaryTerm.getLocale());

         // check if there's existing term with same content, overrides comments
         HGlossaryTerm hGlossaryTerm = getOrCreateGlossaryTerm(to, termHLocale, glossaryTerm.getContent());

         hGlossaryTerm.getComments().clear();

         for (String comment : glossaryTerm.getComments())
         {
            hGlossaryTerm.getComments().add(new HTermComment(comment));
         }
      }
      glossaryDAO.makePersistent(to);
   }

   public void transferEntriesResource(List<HGlossaryEntry> hGlosssaryEntries, Glossary glossary)
   {
      for (HGlossaryEntry hGlossaryEntry : hGlosssaryEntries)
      {
         GlossaryEntry glossaryEntry = new GlossaryEntry();
         glossaryEntry.setSourcereference(hGlossaryEntry.getSourceRef());
         glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());

         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            GlossaryTerm glossaryTerm = new GlossaryTerm();
            glossaryTerm.setContent(hGlossaryTerm.getContent());
            glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());

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
         glossaryEntry.setSrcLang(hGlossaryEntry.getSrcLocale().getLocaleId());
         glossaryEntry.setSourcereference(hGlossaryEntry.getSourceRef());
         for (HGlossaryTerm hGlossaryTerm : hGlossaryEntry.getGlossaryTerms().values())
         {
            if (hGlossaryTerm.getLocale().getLocaleId().equals(locale))
            {
               GlossaryTerm glossaryTerm = new GlossaryTerm();
               glossaryTerm.setContent(hGlossaryTerm.getContent());
               glossaryTerm.setLocale(hGlossaryTerm.getLocale().getLocaleId());

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
