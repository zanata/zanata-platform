package org.zanata.rest.enunciate;

import org.zanata.rest.MediaTypes;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(GlossaryResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML, MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON })
interface GlossaryResource extends org.zanata.rest.service.GlossaryResource
{
}
