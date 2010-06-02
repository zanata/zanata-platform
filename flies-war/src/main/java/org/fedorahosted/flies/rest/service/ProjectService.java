package org.fedorahosted.flies.rest.service;

import java.io.InputStream;
import java.net.URI;

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
import javax.ws.rs.core.UriInfo;

import org.fedorahosted.flies.dao.AccountDAO;
import org.fedorahosted.flies.dao.ProjectDAO;
import org.fedorahosted.flies.model.HAccount;
import org.fedorahosted.flies.model.HIterationProject;
import org.fedorahosted.flies.model.HProject;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.model.validator.SlugValidator;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.AbstractProject;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIterationInline;
import org.fedorahosted.flies.rest.dto.ProjectRes;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;

@Name("projectService")
@Path(ProjectService.SERVICE_PATH)
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class ProjectService {

	
	public static final String PROJECT_SLUG_TEMPLATE = "{projectSlug:" + SlugValidator.PATTERN + "}";
	public static final String SERVICE_PATH = "/projects/p/" + PROJECT_SLUG_TEMPLATE; 
	
	@PathParam("projectSlug")
	String projectSlug;

	@HeaderParam(HttpHeaderNames.ACCEPT)
	@DefaultValue(MediaType.APPLICATION_XML)
	private MediaType accept;

	@Context
	private UriInfo uri;

	@HeaderParam("Content-Type")
	private MediaType requestContentType;

	@Context
	private HttpHeaders headers;

	@Context
	private Request request;
	
	Log log = Logging.getLog(ProjectService.class);

	@In
	ProjectDAO projectDAO;

	@In
	AccountDAO accountDAO;

	@In Identity identity;
	
	@In
	ETagUtils eTagUtils;
	
	public ProjectService() {
	}

	public ProjectService(ProjectDAO projectDAO, AccountDAO accountDAO, Identity identity, ETagUtils eTagUtils) {
		this.projectDAO = projectDAO;
		this.accountDAO = accountDAO;
		this.eTagUtils = eTagUtils;
		this.identity = identity;
	}
	
	@HEAD
	public Response head() {
		EntityTag etag = eTagUtils.generateTagForProject(projectSlug);
		ResponseBuilder response = request.evaluatePreconditions(etag);
		if (response != null) {
			return response.build();
		}
		return Response.ok().tag(etag).build();
	}
	
	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_JSON})
	public Response get() {
		EntityTag etag = eTagUtils.generateTagForProject(projectSlug);

		ResponseBuilder response = request.evaluatePreconditions(etag);
		if (response != null) {
			return response.build();
		}

		HProject hProject = projectDAO.getBySlug(projectSlug);

		ProjectRes project = toResource(hProject, accept);
		return Response.ok(project).tag(etag).build();
	}

	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_JSON})
	@Restrict("#{identity.loggedIn}")
	public Response put(InputStream messageBody) {

		ResponseBuilder response;
		EntityTag etag;

		HProject hProject = projectDAO.getBySlug(projectSlug);

		if (hProject == null) { // must be a create operation
			response = request.evaluatePreconditions();
			if (response != null) {
				return response.build();
			}
			hProject = new HIterationProject();
			hProject.setSlug(projectSlug);

			response = Response.created(uri.getAbsolutePath());
		} else { // must be an update operation
			etag = eTagUtils.generateTagForProject(projectSlug);
			response = request.evaluatePreconditions(etag);
			if (response != null) {
				return response.build();
			}

			response = Response.ok();
		}

		Project project = RestUtils.unmarshall(Project.class, messageBody,
				requestContentType, headers.getRequestHeaders());
		transfer(project, hProject);

		hProject = projectDAO.makePersistent(hProject);
		projectDAO.flush();
		
		if (identity != null && hProject.getMaintainers().isEmpty() ) {
			HAccount hAccount = accountDAO.getByUsername(
					identity.getCredentials().getUsername());
			if (hAccount != null && hAccount.getPerson() != null) {
				hProject.getMaintainers().add(hAccount.getPerson());
			}
			projectDAO.flush();
		}

		etag = eTagUtils.generateTagForProject(projectSlug);
		return response.tag(etag).build();

	}

	public static void transfer(Project from, HProject to) {
		to.setName(from.getName());
		to.setDescription(from.getDescription());
	}

	public static void transfer(HProject from, AbstractProject to) {
		to.setId(from.getSlug());
		to.setName(from.getName());
		to.setDescription(from.getDescription());
	}

	public static ProjectRes toResource(HProject hProject, MediaType mediaType) {
		ProjectRes project = new ProjectRes();
		transfer(hProject, project);
		if (hProject instanceof HIterationProject) {
			HIterationProject itProject = (HIterationProject) hProject;
			for (HProjectIteration pIt : itProject.getProjectIterations()) {
				ProjectIterationInline iteration = new ProjectIterationInline();
				ProjectIterationService.transfer(pIt, iteration);
				iteration
						.getLinks()
						.add(new Link(
								URI.create("iterations/i/" + pIt.getSlug()),
								"self",
								MediaTypes
										.createFormatSpecificType(
												MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION,
												mediaType)));
				project.getIterations().add(iteration);
			}
		}

		return project;
	}

}
