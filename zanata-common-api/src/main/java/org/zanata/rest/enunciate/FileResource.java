package org.zanata.rest.enunciate;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(FileResource.SERVICE_PATH)
@org.codehaus.enunciate.modules.jersey.ExternallyManagedLifecycle
@Produces({ MediaType.APPLICATION_OCTET_STREAM })
@Consumes({ MediaType.APPLICATION_OCTET_STREAM })
interface FileResource extends org.zanata.rest.service.FileResource {
}
