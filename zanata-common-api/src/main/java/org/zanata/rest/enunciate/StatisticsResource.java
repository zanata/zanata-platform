package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(StatisticsResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface StatisticsResource extends org.zanata.rest.service.StatisticsResource
{
}
