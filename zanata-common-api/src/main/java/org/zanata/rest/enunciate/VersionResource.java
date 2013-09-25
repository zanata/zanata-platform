package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(VersionResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface VersionResource extends org.zanata.rest.service.VersionResource
{
}
