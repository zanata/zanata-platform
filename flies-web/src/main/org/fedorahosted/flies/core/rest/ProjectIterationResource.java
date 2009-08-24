package org.fedorahosted.flies.core.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.fedorahosted.flies.core.model.ProjectIteration;
import org.jboss.seam.annotations.Name;

@Name("projectIterationResource")
public class ProjectIterationResource {

	private ProjectIteration projectIteration;
	
	public void setProjectIteration(ProjectIteration projectIteration) {
		this.projectIteration = projectIteration;
	}
	
	@Produces("text/plain")
	@GET
	public String get(){
		return projectIteration.getName();
	}
	
}
