package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.fedorahosted.flies.core.dao.AccountDAO;
import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.HAccount;
import org.fedorahosted.flies.core.model.HProject;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectList;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;

@Name("projectsService")
@Path("/projects")
public class ProjectsService {

	@In
	ProjectDAO projectDAO;
	
	@In
	Session session;
	
	@In 
	AccountDAO accountDAO;
	
	
	@Context
	HttpServletResponse response;
	
	@In ProjectsServiceAction projectsServiceAction;
	
	@GET
	@Produces({ MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaType.APPLICATION_JSON })
	public ProjectList get() {

		return projectsServiceAction.get();
	}
	
	@POST
	@Consumes({ MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaType.APPLICATION_JSON })
	@Restrict("#{identity.loggedIn}")
	public Response post(Project project) throws URISyntaxException{
		
		HProject hProject = projectDAO.getBySlug(project.getId());
		if(hProject != null){
			return Response.status(409).build();
		}
		hProject = new org.fedorahosted.flies.core.model.HIterationProject();
		hProject.setSlug(project.getId());
		hProject.setName(project.getName());
		hProject.setDescription(project.getDescription());
		HAccount hAccount = accountDAO.getByUsername(Identity.instance().getCredentials().getUsername());
		if(hAccount != null && hAccount.getPerson() != null) {
			hProject.getMaintainers().add(hAccount.getPerson());
		}
		
		try{
			session.save(hProject);
			return Response.created( new URI("/projects/p/"+hProject.getSlug()) ).build();
		}
        catch(InvalidStateException e){
        	return Response.status(Status.BAD_REQUEST).build();
        }
        catch(HibernateException e){
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
	}
}
