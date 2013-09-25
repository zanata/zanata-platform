package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(ProjectIterationResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface ProjectIterationResource extends org.zanata.rest.service.ProjectIterationResource
{
}
