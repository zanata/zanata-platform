package org.zanata.rest.enunciate;

import javax.ws.rs.Path;

@Path(GlossaryResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface GlossaryResource extends org.zanata.rest.service.GlossaryResource
{
}
