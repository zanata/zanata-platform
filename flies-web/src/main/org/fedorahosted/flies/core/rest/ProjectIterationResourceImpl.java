package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.fedorahosted.flies.core.model.ProjectIteration;
import org.jboss.seam.annotations.Name;

@Name("projectIterationResource")
public class ProjectIterationResourceImpl implements ProjectIterationResource{

	private ProjectIteration projectIteration;
	
	public void setProjectIteration(ProjectIteration projectIteration) {
		this.projectIteration = projectIteration;
	}
	
	@Override
	public String get(){
		return projectIteration.getName();
	}
	
	
	// hack to allow sub-resource in resteasy
	public static ProjectIterationResource getProxyWrapper(final ProjectIterationResource instance){
		return new ProjectIterationResource(){

			@Override
			public String get() {
				return instance.get();
			}
			
		};
	}	
	
}
