package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.hibernate.Session;
import org.jboss.resteasy.spi.NotFoundException;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("iterationProjectResource")
@Path("/")
public class IterationProjectResourceImpl implements IterationProjectResource{

	@In 
	Session session;
	
	IterationProject project;
	
	@In 
	ProjectIterationDAO projectIterationDAO;
	
	@Override
	public String get(){
		return project.getName();
	}
	
	// hack to allow sub-resource in resteasy
	private IterationProjectResource proxy; 
	public IterationProjectResource getProxyWrapper(){
		if(proxy == null){
			final IterationProjectResourceImpl instance = this;
			proxy = new IterationProjectResource(){
				
				@Override
				public Object getProjectIteration(String iterationSlug) {
					return instance.getProjectIteration(iterationSlug);
				}
				
				@Override
				public String get() {
					return instance.get();
				}
			};
		}
		return proxy;
	}

	@Override
	public Object getProjectIteration(String iterationSlug) {

		ProjectIteration projectIteration = projectIterationDAO.getBySlug(project, iterationSlug);
		if(projectIteration == null){
			throw new NotFoundException("Project Iteration not found: "+iterationSlug);
		}
		
		ProjectIterationResourceImpl piRes = 
			(ProjectIterationResourceImpl) Component.getInstance(ProjectIterationResourceImpl.class, true);
		piRes.setProjectIteration(projectIteration);
		return piRes;

	}

	public void setProject(IterationProject project) {
		this.project = project;
	}
	
}
