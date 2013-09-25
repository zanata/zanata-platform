package org.zanata.rest.enunciate;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(AccountResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
interface AccountResource extends org.zanata.rest.service.AccountResource
{
}
