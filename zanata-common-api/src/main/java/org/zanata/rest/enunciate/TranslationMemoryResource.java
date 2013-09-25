package org.zanata.rest.enunciate;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(TranslationMemoryResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
@Produces( { MediaType.APPLICATION_XML /*, "application/x-tmx"*/ })
@Consumes( { MediaType.APPLICATION_XML /*, "application/x-tmx"*/ })
interface TranslationMemoryResource extends org.zanata.rest.service.TranslationMemoryResource
{
}
