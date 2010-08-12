package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Project;
import org.jboss.resteasy.client.ClientResponse;

//@Path("/projects/p/{projectSlug}")
public interface IProjectResource
{

   @GET
   @Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON })
   public ClientResponse<Project> get();

   @PUT
   @Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON })
   public ClientResponse put(Project project);

}
