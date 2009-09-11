package org.fedorahosted.flies.rest.service;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HIterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.ProjectIterationRef;
import org.fedorahosted.flies.rest.dto.ProjectIterationRefs;
import org.fedorahosted.flies.rest.dto.ProjectRef;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.hibernate.HibernateException;
import org.hibernate.validator.InvalidStateException;
import org.hibernate.Session;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("projectResource")
@Path("/projects")
public class ProjectService{

	@Logger
	Log log;
	
	@In
	ProjectDAO projectDAO;
	
	@In 
	AccountDAO accountDAO;
	
	@In
	Session session;
	
	@Context
	HttpServletResponse response;

	@Context 
	HttpServletRequest request;

	@GET
	@Path("/p/{projectSlug}")
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public org.fedorahosted.flies.rest.dto.Project getProject(
			@PathParam("projectSlug") String projectSlug) {

		org.fedorahosted.flies.core.model.Project p = projectDAO.getBySlug(projectSlug);
		if(p == null)
			throw new WebApplicationException(Status.NOT_FOUND);
		
		return toMini(p);
	}

	private static Project toMini(org.fedorahosted.flies.core.model.Project p){
		Project proj = new Project();
		proj.setId(p.getSlug());
		proj.setName(p.getName());
		proj.setDescription(p.getDescription());
		if(p instanceof HIterationProject){
			HIterationProject itProject = (HIterationProject) p;
			for(ProjectIteration pIt : itProject.getProjectIterations()){
				proj.getIterations().add(
						new ProjectIterationRef(
								new org.fedorahosted.flies.rest.dto.ProjectIteration(
										pIt.getSlug(),
										pIt.getName(), 
										pIt.getDescription()
								)
						)
					);
			}
		}
		
		return proj;
	}
	
	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaType.APPLICATION_JSON })
	public ProjectRefs getProjects() {
		ProjectRefs projectRefs = new ProjectRefs();
		
		List<org.fedorahosted.flies.core.model.Project> projects = session.createQuery("select p from Project p").list();
		
		for(org.fedorahosted.flies.core.model.Project p : projects){
			org.fedorahosted.flies.rest.dto.Project proj = 
				new org.fedorahosted.flies.rest.dto.Project(p.getSlug(), p.getName(), p.getDescription());
			projectRefs.getProjects().add( new ProjectRef( proj ));
		}
		
		return projectRefs;
	}

	@POST
	@Path("/p/{projectSlug}")
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public Response updateProject(@PathParam("projectSlug") String projectSlug, org.fedorahosted.flies.rest.dto.Project project){
			
		org.fedorahosted.flies.core.model.Project p = projectDAO.getBySlug(projectSlug);

		if(p == null)
			throw new WebApplicationException(Status.NOT_FOUND);

		p.setName(project.getName());
		p.setDescription(project.getDescription());
		try{
			session.flush();
			return Response.ok().build();
		}
		catch(HibernateException e){
			return Response.notAcceptable(null).build();
		}
	}
	
	@PUT
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	public Response addProject(org.fedorahosted.flies.rest.dto.Project project) throws URISyntaxException{
		
		org.fedorahosted.flies.core.model.Project p = projectDAO.getBySlug(project.getId());
		if(p != null){
			return Response.status(409).build();
		}
		p = new org.fedorahosted.flies.core.model.HIterationProject();
		p.setSlug(project.getId());
		p.setName(project.getName());
		p.setDescription(project.getDescription());
		String apiKey = request.getHeader(FliesRestSecurityInterceptor.X_AUTH_TOKEN_HEADER);
		if(apiKey != null) {
			HAccount account = accountDAO.getByApiKey(apiKey);
			if(account != null && account.getPerson() != null) {
				p.getMaintainers().add(account.getPerson());
			}
		}
		try{
			session.save(p);
			return Response.created( new URI("/projects/p/"+p.getSlug()) ).build();
		}
        catch(InvalidStateException e){
        	return Response.status(Status.BAD_REQUEST).build();
        }
        catch(HibernateException e){
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
	
	
}
