package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(FileResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface FileResource extends org.zanata.rest.service.FileResource
{
}
