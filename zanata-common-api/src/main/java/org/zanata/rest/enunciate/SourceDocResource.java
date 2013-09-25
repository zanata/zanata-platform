package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(SourceDocResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface SourceDocResource extends org.zanata.rest.service.SourceDocResource
{
}
