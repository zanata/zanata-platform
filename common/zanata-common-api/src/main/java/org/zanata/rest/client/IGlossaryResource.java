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

@Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@Consumes({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
public interface IGlossaryResource
{
   public static final String SERVICE_PATH = "/glossary";

   @GET
   public ClientResponse<Glossary> getEntries();

   @GET
   @Path("{locale}")
   public ClientResponse<Glossary> get(@PathParam("locale") LocaleId locale);

   @PUT
   public ClientResponse<Glossary> put(Glossary glossary);

   @DELETE
   @Path("{locale}")
   public ClientResponse<String> deleteGlossary(@PathParam("locale") LocaleId locale);

   @DELETE
   public ClientResponse<String> deleteGlossaries();

}
