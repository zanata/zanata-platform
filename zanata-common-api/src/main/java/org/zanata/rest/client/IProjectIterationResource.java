package org.zanata.rest.client;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.service.ProjectIterationResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//TODO remove the template parameters from ProjectIterationResource's Path
//@Path(ProjectIterationResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IProjectIterationResource extends ProjectIterationResource {

    @Override
    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ClientResponse<ProjectIteration> get();

    @Override
    @PUT
    @Consumes({ MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ClientResponse put(ProjectIteration project);

    @GET
    @Path("/config")
    @Produces({ MediaType.APPLICATION_XML })
    @Override
    ClientResponse sampleConfiguration();
}
