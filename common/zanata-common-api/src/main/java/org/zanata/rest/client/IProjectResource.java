package org.zanata.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.ProjectResource;

//@Path("/projects/p/{projectSlug}")
public interface IProjectResource extends ProjectResource
{

   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON })
   public ClientResponse<Project> get();

   @PUT
   @Consumes( { MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON })
   public ClientResponse put(Project project);

}
