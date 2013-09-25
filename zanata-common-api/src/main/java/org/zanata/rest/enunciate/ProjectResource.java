package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(ProjectResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface ProjectResource extends org.zanata.rest.service.ProjectResource
{
}
