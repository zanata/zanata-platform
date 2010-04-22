package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.core.model.HProjectIteration;
import org.fedorahosted.flies.core.model.HProjectSeries;
import org.fedorahosted.flies.repository.model.HProjectContainer;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.AbstractMiniProjectIteration;
import org.fedorahosted.flies.rest.dto.AbstractProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIteration;
import org.fedorahosted.flies.rest.dto.ProjectIterationRes;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Name("projectIterationService")
@Path("/projects/p/{projectSlug}/iterations/i/{iterationSlug}")
public class ProjectIterationService {

	private static Logger log = LoggerFactory.getLogger(ProjectIterationService.class);
	
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

	@GET
	@Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON,
			MediaType.APPLICATION_JSON })
	public ProjectIterationRes get() {

		org.fedorahosted.flies.core.model.HProjectIteration hProjectIteration = projectIterationDAO
				.getBySlug(projectSlug, iterationSlug);

		if (hProjectIteration == null)
			throw new NotFoundException("No such Project Iteration");

		return toResource(hProjectIteration);
	}

	public static void transfer(HProjectIteration from, AbstractMiniProjectIteration to) {
		to.setId(from.getSlug());
		to.setName(from.getName());
	}
	
	public static void transfer(HProjectIteration from, AbstractProjectIteration to) {
		transfer(from, (AbstractMiniProjectIteration)to);
		to.setDescription(from.getDescription());
	}
	
	public static ProjectIterationRes toResource(HProjectIteration hibIt) {
		ProjectIterationRes it = new ProjectIterationRes();
		transfer(hibIt, it);
		return it;

	}

	@PUT
	@Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_XML,
			MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION_JSON,
			MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response put(ProjectIteration projectIteration) {

		HProjectIteration hProjectIteration = projectIterationDAO
				.getBySlug(projectSlug, projectIteration.getId());

		HProject hProject = projectDAO.getBySlug(projectSlug);

		if (hProjectIteration != null || hProject == null) {
			return Response.status(409).build();
		}

		hProjectIteration = new HProjectIteration();
		hProjectIteration.setProject((HIterationProject) hProject);
		hProjectIteration.setName(projectIteration.getName());
		hProjectIteration.setSlug(projectIteration.getId());
		hProjectIteration.setDescription(projectIteration.getDescription());

		try {
			HProjectContainer container = new HProjectContainer();
			session.save(container);
			hProjectIteration.setContainer(container);
			hProjectIteration.setProjectSeries((HProjectSeries) session.load(
					HProjectSeries.class, 1l));
			session.save(hProjectIteration);
			return Response.created(
					URI.create("/i/" + hProjectIteration.getSlug())).build();
		} catch (InvalidStateException e) {
			String message = "Iteration with id '" + projectIteration.getId()
				+ "' is invalid: "
				+ Arrays.asList(e.getInvalidValues());
			log.warn(message + '\n' + projectIteration, e);
			return Response.status(Status.BAD_REQUEST).entity(message)
					.build();
		} catch (HibernateException e) {
			log.error("Hibernate exception", e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Server error").build();
		}

	}
}
