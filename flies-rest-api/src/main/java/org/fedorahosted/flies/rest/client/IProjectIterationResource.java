package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationRes;
import org.jboss.resteasy.client.ClientResponse;

//@Path("/i/{iterationSlug}")
public interface IProjectIterationResource{

	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON })
	public ClientResponse<ProjectIterationRes> get();

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON })
	public Response put(
			ProjectIteration project);
	
//	@Path("/documents")
//	public IDocumentsResource getDocuments();
}
