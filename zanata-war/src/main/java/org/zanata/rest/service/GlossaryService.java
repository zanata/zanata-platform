package org.zanata.rest.service;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HTermComment;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.seam.resteasy.IgnoreInterfacePath;
import org.zanata.service.GlossaryFileService;

@Name("glossaryService")
@Path(GlossaryService.SERVICE_PATH)
@Transactional
@IgnoreInterfacePath
public class GlossaryService implements GlossaryResource
{
   @Context
   private UriInfo uri;

   @HeaderParam("Content-Type")
   @Context
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   @In
   private GlossaryDAO glossaryDAO;

   @In
   private GlossaryFileService glossaryFileServiceImpl;

   Log log = Logging.getLog(GlossaryService.class);

   @Override
   @GET
   @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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
   @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response get(@PathParam("locale")
   LocaleId locale)
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
   @Consumes({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @Restrict("#{s:hasPermission('', 'glossary-insert')}")
   public Response put(Glossary glossary)
   {
      ResponseBuilder response;

      // must be a create operation
      response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      response = Response.created(uri.getAbsolutePath());

      glossaryFileServiceImpl.saveGlossary(glossary);

      return response.build();
   }

   @Override
   @DELETE
   @Path("/{locale}")
   @Restrict("#{s:hasPermission('', 'glossary-delete')}")
   public Response deleteGlossary(@PathParam("locale") LocaleId targetLocale)
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }

      int rowCount = glossaryDAO.deleteAllEntries(targetLocale);
      log.info("Glossary delete (" + targetLocale + "): " + rowCount);

      return Response.ok().build();
   }

   @Override
   @DELETE
   @Restrict("#{s:hasPermission('', 'glossary-delete')}")
   public Response deleteGlossaries()
   {
      ResponseBuilder response = request.evaluatePreconditions();
      if (response != null)
      {
         return response.build();
      }
      int rowCount = glossaryDAO.deleteAllEntries();
      log.info("Glossary delete all: " + rowCount);

      return Response.ok().build();
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
