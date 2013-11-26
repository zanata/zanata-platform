package org.zanata.rest.client;

import org.zanata.rest.service.AsynchronousProcessResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(AsynchronousProcessResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IAsynchronousProcessResource extends
        AsynchronousProcessResource {
}
