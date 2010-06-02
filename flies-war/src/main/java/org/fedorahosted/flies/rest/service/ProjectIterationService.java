package org.fedorahosted.flies.rest.service;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.fedorahosted.flies.dao.ProjectDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HIterationProject;
import org.fedorahosted.flies.model.HProject;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.model.validator.SlugValidator;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.AbstractMiniProjectIteration;
import org.fedorahosted.flies.rest.dto.AbstractProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationRes;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;

@Name("projectIterationService")
@Path(ProjectIterationService.SERVICE_PATH)
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProjectIterationService {

	public static final String SERVICE_PATH = 
		ProjectService.SERVICE_PATH + "/iterations/i/{iterationSlug:" + SlugValidator.PATTERN + "}";
	
	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;
	
	@HeaderParam("Content-Type")
	private MediaType requestContentType;
	
	@Context
	private HttpHeaders headers;
	
	@HeaderParam(HttpHeaderNames.ACCEPT)
	@DefaultValue(MediaType.APPLICATION_XML)
	private MediaType accept;
	
	@Context
	private UriInfo uri;
	
	@Context
	private Request request;

	@In
	ProjectDAO projectDAO;

	@In
	ProjectIterationDAO projectIterationDAO;

	@In
	ETagUtils eTagUtils;

	public ProjectIterationService() {
	}
	
	public ProjectIterationService(ProjectDAO projectDAO, ProjectIterationDAO projectIterationDAO, ETagUtils eTagUtils) {
		this.projectDAO = projectDAO;
		this.projectIterationDAO = projectIterationDAO;
		this.eTagUtils = eTagUtils;
	}
	
	@HEAD
	public Response head() {
		EntityTag etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);

		ResponseBuilder response = request.evaluatePreconditions(etag);
		if(response != null) {
			return response.build();
		}

		return Response.ok().tag(etag).build();
	}
	
	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON})
	public Response get() {

		EntityTag etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);

		ResponseBuilder response = request.evaluatePreconditions(etag);
		if(response != null) {
			return response.build();
		}
		
		HProjectIteration hProjectIteration = projectIterationDAO
				.getBySlug(projectSlug, iterationSlug);

		ProjectIterationRes it = new ProjectIterationRes();
		transfer(hProjectIteration, it);
		
		return Response.ok(it).tag(etag).build();
	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON})
	@Restrict("#{identity.loggedIn}")
	public Response put(InputStream messageBody) {
		
		ResponseBuilder response;
		EntityTag etag;
		
		HProjectIteration hProjectIteration = projectIterationDAO
			.getBySlug(projectSlug, iterationSlug);
		
		if(hProjectIteration == null) { // must be a create operation
			response = request.evaluatePreconditions();
			if(response != null) {
				return response.build();
			}
			HProject hProject = projectDAO.getBySlug(projectSlug);
			if(hProject == null) {
				return Response.status(Status.NOT_FOUND)
					.entity("Project '"+ projectSlug +"' not found.")
					.build();
			}
			hProjectIteration = new HProjectIteration();
			hProjectIteration.setSlug(iterationSlug);
			hProjectIteration.setProject((HIterationProject) hProject);
			response = Response.created( uri.getAbsolutePath() );
			
		}
		else{ // must be an update operation
			etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);
			response = request.evaluatePreconditions(etag);
			if(response != null) {
				return response.build();
			}
			response = Response.ok();
		}

		ProjectIteration projectIteration = RestUtils.unmarshall(ProjectIteration.class, messageBody, requestContentType, headers.getRequestHeaders());
		transfer(projectIteration, hProjectIteration);
		
		projectIterationDAO.makePersistent(hProjectIteration);
		projectIterationDAO.flush();

		etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);
		return response.tag(etag).build();
		
	}

	public static void transfer(ProjectIteration from, HProjectIteration to) {
		to.setName(from.getName());
		to.setDescription(from.getDescription());
	}
	
	public static void transfer(HProjectIteration from, AbstractMiniProjectIteration to) {
		to.setId(from.getSlug());
		to.setName(from.getName());
	}
	
	public static void transfer(HProjectIteration from, AbstractProjectIteration to) {
		transfer(from, (AbstractMiniProjectIteration)to);
		to.setDescription(from.getDescription());
	}

}
