package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(TranslatedDocResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface TranslatedDocResource extends org.zanata.rest.service.TranslatedDocResource
{
}
