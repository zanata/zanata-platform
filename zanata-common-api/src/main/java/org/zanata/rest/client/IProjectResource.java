package org.zanata.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.ProjectResource;

//@Path("/projects/p/{projectSlug}")
public interface IProjectResource extends ProjectResource
{

   @HEAD
   @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public ClientResponse head();
   
   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON })
   public ClientResponse<Project> get();

   @PUT
   @Consumes( { MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON })
   public ClientResponse put(Project project);
}
