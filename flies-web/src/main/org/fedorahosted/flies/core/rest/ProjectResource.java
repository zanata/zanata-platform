package org.fedorahosted.flies.core.rest;

import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import java.lang.String;
import java.net.URI;
import java.net.URISyntaxException;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.fedorahosted.flies.core.rest.api.IterationProject;
import org.fedorahosted.flies.core.rest.api.MetaProject;
import org.fedorahosted.flies.core.rest.api.MetaProject.ProjectType;
import org.hibernate.Session;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.w3c.dom.Element;

@Name("projectResource")
@Path("/project")
public class ProjectResource {

	@Logger
	Log log;
	
	@In
	ProjectDAO projectDAO;
	
	@In(value="#{entityManager.delegate}")
	Session session;
	
	@Context
	HttpServletResponse response;

	@Context 
	HttpServletRequest request;
	
	@GET
	@Path("/{projectSlug}")
	@Produces("application/atom+xml")
	public Entry getProject(@PathParam("projectSlug") String projectSlug) {
		checkPermissions();
		Project p = projectDAO.getBySlug(projectSlug);
		if(p == null)
			throw new NotFoundException("Project not found: "+projectSlug);

		IterationProject project = new IterationProject(p);

		Entry entry = create(project);
		return entry;
	}

	private void checkPermissions(){
		String authToken = request.getHeader("X-Auth-Token");
		log.info("Attempted to authenticate with token {0}", authToken);
		if(!"bob".equals(authToken)){
			throw new UnauthorizedException();
		}
	}
	
	@POST
	@Path("/{projectSlug}")
	@Consumes("application/atom+xml")
	@Transactional
	public void updateProject(@PathParam("projectSlug") String projectSlug, Entry entry) {
		Project p = projectDAO.getBySlug(projectSlug);
		if(p == null)
			throw new NotFoundException("Project not found: "+projectSlug);
		try{
			Content content = entry.getContent();
			MetaProject apiProject = content.getJAXBObject(MetaProject.class);
			
			session.saveOrUpdate(p);
		}
		catch(JAXBException e){
			throw new BadRequestException("failed to parse content");
		}
	}
	
	private static Entry create(MetaProject project){
		Entry entry = new Entry();
		entry.setTitle(project.getName());
		entry.setSummary(project.getDescription());
		entry.setUpdated(new Date());
		try {
			entry.setId(new URI(project.getId()));
		} catch (URISyntaxException e) {
		}
		Content content = new Content();
		entry.setContent(content);
		content.setType(MediaType.APPLICATION_XML);
		content.setJAXBObject(project);
		return entry;
	}

	@GET
	@Produces("application/atom+xml")
	public Feed getProjects() {
		
		List<MetaProject> projects = session.createQuery("select new org.fedorahosted.flies.core.rest.api.MetaProject(p) from Project p")
			.list();
		
		Feed projectFeed = new Feed();
		projectFeed.setTitle("projects");
		projectFeed.setUpdated(new Date());
		for(MetaProject p: projects){
			Entry entry = create(p);
			projectFeed.getEntries().add(entry);
		}
		return projectFeed;
	}
	

}
