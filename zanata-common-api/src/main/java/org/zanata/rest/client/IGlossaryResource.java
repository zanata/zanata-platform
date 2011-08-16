package org.zanata.rest.client;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.common.LocaleId;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryTerm;

@Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@Consumes({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
public interface IGlossaryResource
{
   public static final String SERVICE_PATH = "/glossary";

   @GET
   @Path(SERVICE_PATH)
   public ClientResponse<Glossary> getEntries();

   @GET
   @Path(SERVICE_PATH + "/{locale}")
   public ClientResponse<Glossary> get(@PathParam("locale") LocaleId locale);

   @PUT
   @Path(SERVICE_PATH)
   public ClientResponse<Glossary> put(Glossary glossary);

}
