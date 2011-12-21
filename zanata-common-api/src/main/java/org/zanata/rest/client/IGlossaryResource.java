package org.zanata.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.common.LocaleId;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.service.GlossaryResource;

@Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@Consumes({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
public interface IGlossaryResource extends GlossaryResource
{
   public static final String SERVICE_PATH = "/glossary";

   @Override
   @GET
   public ClientResponse<Glossary> getEntries();

   @Override
   @GET
   @Path("{locale}")
   public ClientResponse<Glossary> get(@PathParam("locale") LocaleId locale);

   @Override
   @PUT
   public ClientResponse<Glossary> put(Glossary glossary);

   @Override
   @DELETE
   @Path("{locale}")
   public ClientResponse<String> deleteGlossary(@PathParam("locale") LocaleId locale);

   @Override
   @DELETE
   public ClientResponse<String> deleteGlossaries();

}
