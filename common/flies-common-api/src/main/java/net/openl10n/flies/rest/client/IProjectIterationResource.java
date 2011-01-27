package net.openl10n.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.dto.ProjectIteration;

import org.jboss.resteasy.client.ClientResponse;

//@Path("/i/{iterationSlug}")
public interface IProjectIterationResource
{

   @GET
   @Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON })
   public ClientResponse<ProjectIteration> get();

   @PUT
   @Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON })
   public ClientResponse put(ProjectIteration project);

   // @Path("/documents")
   // public IDocumentsResource getDocuments();
}
