package org.zanata.rest.client;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.ProjectResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

//TODO remove the template parameters from ProjectResource's Path
//@Path(ProjectResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IProjectResource extends ProjectResource {

    @HEAD
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ClientResponse head();

    @GET
    @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ClientResponse<Project> get();

    @PUT
    @Consumes({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
            MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
            MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public ClientResponse put(Project project);
}
