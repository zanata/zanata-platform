package org.zanata.rest.client;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;

@Path("/projects")
public interface IProjectsResource
{

   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECTS_XML, MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON })
   public ClientResponse<List<Project>> get();

}
