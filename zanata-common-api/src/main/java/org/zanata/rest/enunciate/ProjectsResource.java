package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(ProjectsResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface ProjectsResource extends org.zanata.rest.service.ProjectsResource
{
}
