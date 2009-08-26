package org.fedorahosted.flies.rest.impl;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fedorahosted.flies.core.dao.ProjectIterationDAO;
import org.fedorahosted.flies.core.model.IterationProject;
import org.fedorahosted.flies.core.model.ProjectIteration;
import org.fedorahosted.flies.rest.api.IterationProjectResource;
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
	public static IterationProjectResource getProxyWrapper(final IterationProjectResource instance){
		return new IterationProjectResource(){
			
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

	@Override
	public Object getProjectIteration(String iterationSlug) {

		ProjectIteration projectIteration = projectIterationDAO.getBySlug(project, iterationSlug);
		if(projectIteration == null){
			throw new NotFoundException("Project Iteration not found: "+iterationSlug);
		}
		
		ProjectIterationResourceImpl piRes = 
			(ProjectIterationResourceImpl) Component.getInstance(ProjectIterationResourceImpl.class, true);
		piRes.setProjectIteration(projectIteration);
		return ProjectIterationResourceImpl.getProxyWrapper(piRes);

	}

	public void setProject(IterationProject project) {
		this.project = project;
	}
	
}
