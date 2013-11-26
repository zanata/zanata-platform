package org.zanata.rest.client;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.jboss.resteasy.client.ClientResponse;
import org.zanata.common.LocaleId;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.service.GlossaryResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(GlossaryResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
public interface IGlossaryResource extends GlossaryResource {
    public static final String SERVICE_PATH = "/glossary";

    @Override
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
            MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(Glossary.class)
    public ClientResponse<Glossary> getEntries();

    @Override
    @GET
    @Path("/{locale}")
    @Produces({ MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
            MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @TypeHint(Glossary.class)
    public ClientResponse<Glossary> get(@PathParam("locale") LocaleId locale);

    @Override
    @PUT
    public ClientResponse<String> put(Glossary glossary);

    @Override
    @DELETE
    @Path("/{locale}")
    public ClientResponse<String> deleteGlossary(
            @PathParam("locale") LocaleId locale);

    @Override
    @DELETE
    public ClientResponse<String> deleteGlossaries();

}
