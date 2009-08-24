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
public class IterationProjectResource implements IIterationProjectResource{

	@In 
	Session session;
	
	IterationProject project;
	
	@In 
	ProjectIterationDAO projectIterationDAO;
	
	@Override
	public String get(){
		return project.getName();
	}
	
	public IIterationProjectResource unwrap(){
		final IterationProjectResource instance = this;
		return new IIterationProjectResource(){
			
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
		
		ProjectIterationResource piRes = 
			(ProjectIterationResource) Component.getInstance(ProjectIterationResource.class, true);
		piRes.setProjectIteration(projectIteration);
		return piRes;

	}

	public void setProject(IterationProject project) {
		this.project = project;
	}
	
}
