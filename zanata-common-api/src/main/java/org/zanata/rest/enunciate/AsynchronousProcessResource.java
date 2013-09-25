package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(AsynchronousProcessResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface AsynchronousProcessResource extends org.zanata.rest.service.AsynchronousProcessResource
{
}
