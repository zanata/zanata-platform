package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(TranslationMemoryResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface TranslationMemoryResource extends org.zanata.rest.service.TranslationMemoryResource
{
}
