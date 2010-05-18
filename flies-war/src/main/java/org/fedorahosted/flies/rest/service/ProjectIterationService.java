package org.fedorahosted.flies.rest.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.dao.ProjectDAO;
import org.fedorahosted.flies.dao.ProjectIterationDAO;
import org.fedorahosted.flies.model.HIterationProject;
import org.fedorahosted.flies.model.HProject;
import org.fedorahosted.flies.model.HProjectIteration;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.AbstractMiniProjectIteration;
import org.fedorahosted.flies.rest.dto.AbstractProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationRes;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;

@Name("projectIterationService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}")
public class ProjectIterationService {

	@Logger
	Log log;
	
	@PathParam("projectSlug")
	private String projectSlug;

	@PathParam("iterationSlug")
	private String iterationSlug;

	@In
	ProjectDAO projectDAO;

	@In
	ProjectIterationDAO projectIterationDAO;

	@In
	Session session;

	@HeaderParam(HttpHeaderNames.ACCEPT)
	@DefaultValue(MediaType.APPLICATION_XML)
	MediaType accept;
	
	@Context
	private UriInfo uri;
	
	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON,
			MediaType.APPLICATION_JSON })
	public Response get(@HeaderParam(HttpHeaderNames.IF_NONE_MATCH) EntityTag ifNoneMatch) {

		EntityTag etag = projectIterationDAO.getETag(projectSlug, iterationSlug);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).build();
		 		
		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}
		
		HProjectIteration hProjectIteration = projectIterationDAO
				.getBySlug(projectSlug, iterationSlug);

		return Response.ok(toResource(hProjectIteration, accept)).tag(etag).build();
	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON,
			MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(ProjectIteration projectIteration,
			 @HeaderParam(HttpHeaderNames.IF_MATCH) EntityTag ifMatch) {
		EntityTag etag = projectIterationDAO.getETag(projectSlug, iterationSlug);
		
		HProjectIteration hProjectIteration;
		
		if(etag == null) {
			// this has to be a create operation
			
			if(ifMatch != null) {
				// the caller expected an existing resource at this location 
				return Response.status(Status.NOT_FOUND).build();
			}
			
			HProject hProject = projectDAO.getBySlug(projectSlug);
			if( hProject == null) {
				return Response.status(Status.NOT_FOUND).build();
			}

			hProjectIteration = new HProjectIteration();
			hProjectIteration.setSlug(projectIteration.getId());
			hProjectIteration.setProject((HIterationProject) hProject);
			transfer(projectIteration, hProjectIteration);
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}
		else{
			hProjectIteration = projectIterationDAO
				.getBySlug(projectSlug, projectIteration.getId());
			transfer(projectIteration, hProjectIteration);
		}

		try {
			ResponseBuilder response;
			if(!session.contains(hProjectIteration)) {
				session.save(hProjectIteration);
				response = Response.created( uri.getAbsolutePath() );
			} else {
				session.flush();
				response = Response.ok();
			}
			etag = projectIterationDAO.getETag(projectSlug, iterationSlug);
			return response.tag(etag).build();
			
		} catch (InvalidStateException e) {
			String message = 
				String.format(
					"Iteration '%s:%s' is invalid: \n %s", 
					projectSlug,iterationSlug, StringUtils.join(e.getInvalidValues(),"\n"));
			log.warn(message + '\n' + projectIteration);
			log.debug(e,e);
			return Response.status(Status.BAD_REQUEST).entity(message)
					.build();
		} catch (HibernateException e) {
			log.error("Hibernate exception", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
		}

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
	
	public static ProjectIterationRes toResource(HProjectIteration hibIt, MediaType mediaType) {
		ProjectIterationRes it = new ProjectIterationRes();
		transfer(hibIt, it);
		return it;

	}

}
