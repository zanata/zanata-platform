package org.fedorahosted.flies.core.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

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
import javax.xml.bind.JAXBException;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.ContentProject;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.rest.api.MetaProject;
import org.hibernate.Session;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;

@Name("projectResource")
@Path("/project/")
public class ProjectResource {

	@Logger
	Log log;
	
	@In
	ProjectDAO projectDAO;
	
	@In
	Session session;
	
	@Context
	HttpServletResponse response;

	@Context 
	HttpServletRequest request;
	
	@Path("/{projectSlug}")
	public Object getProject(@PathParam("projectSlug") String projectSlug) {
		checkPermissions();
		Project p = projectDAO.getBySlug(projectSlug);
		if(p == null)
			throw new NotFoundException("Project not found: "+projectSlug);
		
		if( p instanceof IterationProject){
			IterationProjectResource itPrRes = 
				(IterationProjectResource) Component.getInstance("iterationProjectResource",ScopeType.STATELESS, true);
			itPrRes.setProject((IterationProject) p);
			
			return itPrRes.unwrap();
		}
		else {//else if (p instanceof ContentProject){
			throw new UnauthorizedException("not implemented");
		}
		
	}

	private void checkPermissions(){
		String authToken = request.getHeader("X-Auth-Token");
		log.info("Attempted to authenticate with token {0}", authToken);
		if(!"bob".equals(authToken)){
			throw new UnauthorizedException();
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
	public Feed get() {
		
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
