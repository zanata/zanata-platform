package org.fedorahosted.flies.rest.impl;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.fedorahosted.flies.core.dao.ProjectDAO;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.rest.ProjectResource;
import org.fedorahosted.flies.rest.dto.ProjectRef;
import org.fedorahosted.flies.rest.dto.ProjectRefs;
import org.hibernate.Session;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.resteasy.spi.UnauthorizedException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("projectResource")
@Path("/project/")
public class ProjectResourceImpl implements ProjectResource{

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
	
	@Override
	public Object getProject(String projectSlug) {
		checkPermissions();
		Project p = projectDAO.getBySlug(projectSlug);
		if(p == null)
			throw new NotFoundException("Project not found: "+projectSlug);
		
		if( p instanceof IterationProject){
			IterationProjectResourceImpl itPrRes = 
				(IterationProjectResourceImpl) Component.getInstance(IterationProjectResourceImpl.class, true);
			itPrRes.setProject((IterationProject) p);
			
			return IterationProjectResourceImpl.getProxyWrapper(itPrRes);
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

	@Override
	public ProjectRefs get() {
		ProjectRefs projectRefs = new ProjectRefs();
		
		List<Project> projects = session.createQuery("select p from Project p").list();
		
		for(Project p : projects){
			org.fedorahosted.flies.rest.dto.Project restProj = 
				new org.fedorahosted.flies.rest.dto.Project(p.getSlug(), p.getName(), p.getDescription());
			projectRefs.getProjects().add( new ProjectRef( restProj ));
		}
		
		return projectRefs;
		
	}
	

}
