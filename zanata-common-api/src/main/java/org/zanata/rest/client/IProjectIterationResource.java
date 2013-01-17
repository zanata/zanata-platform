package org.zanata.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.service.ProjectIterationResource;

//@Path("/i/{iterationSlug}")
public interface IProjectIterationResource extends ProjectIterationResource
{

   @Override
   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON })
   public ClientResponse<ProjectIteration> get();

   @Override
   @PUT
   @Consumes( { MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON })
   public ClientResponse put(ProjectIteration project);
}
