package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(CopyTransResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface CopyTransResource extends org.zanata.rest.service.CopyTransResource
{
}
