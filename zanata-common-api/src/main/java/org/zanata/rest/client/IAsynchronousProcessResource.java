package org.zanata.rest.client;

import org.zanata.rest.service.AsynchronousProcessResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IAsynchronousProcessResource extends AsynchronousProcessResource
{
}
