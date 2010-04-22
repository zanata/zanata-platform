package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.security.MessageDigest;

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

import org.fedorahosted.flies.common.ContentType;
import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.AbstractProject;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationInline;
import org.fedorahosted.flies.rest.dto.ProjectRes;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.jboss.resteasy.util.AcceptCharsetParser;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.jboss.seam.util.Hex;

import com.google.gwt.http.client.URL;

@Name("projectService")
@Path("/projects/p/{projectSlug}")
public class ProjectService {

	@PathParam("projectSlug")
	String projectSlug;

	@Logger
	Log log;

	@In
	ProjectDAO projectDAO;

	@In
	Session session;

	@In
	AccountDAO accountDAO;

	@HeaderParam("Accept")
	@DefaultValue(MediaType.APPLICATION_XML)
	MediaType accept;

	@Context
	private UriInfo uri;
	
	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_JSON,
			MediaType.APPLICATION_JSON })
	public Response get(@HeaderParam("If-None-Match") EntityTag ifNoneMatch) {
		EntityTag etag = projectDAO.getETag(projectSlug);

		if(etag == null)
			return Response.status(Status.NOT_FOUND).build();
		 		
		if(ifNoneMatch != null && etag.equals(ifNoneMatch)) {
			return Response.notModified(ifNoneMatch).build();
		}

		HProject hProject = projectDAO.getBySlug(projectSlug);
		
		ProjectRes project = toResource(hProject, accept);
		return Response.ok(project).tag(etag).build();
	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_JSON,
			MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(Project project, @HeaderParam("If-Match") EntityTag ifMatch) {

		EntityTag etag = projectDAO.getETag(projectSlug);
		HProject hProject;
		
		if(etag == null) { 
			// this has to be a create operation
			
			if(ifMatch != null) {
				// the caller expected an existing resource at this location 
				return Response.status(Status.NOT_FOUND).build();
			}
			
			hProject = new org.fedorahosted.flies.core.model.HIterationProject();
			hProject.setSlug(project.getId());
			hProject.setName(project.getName());
			hProject.setDescription(project.getDescription());
		}
		else if(ifMatch != null && !etag.equals(ifMatch)) {
			return Response.status(Status.CONFLICT).build();
		}
		else {
			// it's an update operation
			hProject = projectDAO.getBySlug(project.getId());
			hProject.setSlug(project.getId());
			hProject.setName(project.getName());
			hProject.setDescription(project.getDescription());
		}
		
		try {
			if (!session.contains(hProject)) {
				session.save(hProject);
				session.flush();
				HAccount hAccount = accountDAO.getByUsername(Identity
						.instance().getCredentials().getUsername());
				if (hAccount != null && hAccount.getPerson() != null) {
					hProject.getMaintainers().add(hAccount.getPerson());
				}
				session.flush();
				return Response.created( uri.getAbsolutePath() ).build();
			} else {
				session.flush();
				return Response.ok().build();
			}
		} catch (InvalidStateException e) {
			return Response.status(Status.BAD_REQUEST).build();
		} catch (HibernateException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}

	public static void transfer(HProject from, AbstractProject to){
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
				iteration.getLinks().add(
						new Link( URI.create("iterations/i/"+pIt.getSlug()), "self", 
								MediaTypes.createFormatSpecificType(
										MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION,
										mediaType)
						));
				project.getIterations().add(iteration);
			}
		}

		return project;
	}

}
