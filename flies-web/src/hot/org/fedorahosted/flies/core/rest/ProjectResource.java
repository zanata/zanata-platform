package org.fedorahosted.flies.core.rest;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.ProduceMime;

import org.fedorahosted.flies.core.model.Project;
import org.fedorahosted.flies.core.model.ProjectSeries;
import org.fedorahosted.flies.core.model.ProjectTarget;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

@Name("projectResource")
@Path("/project")
public class ProjectResource {
  
  @In
  private EntityManager entityManager;

  @Out(required = false)
  private Project project;
  
  @GET
  @Path("/{projectId}")
  @ProduceMime("text/plain")
  public Project getProject(@PathParam("projectId") long id) {
     return project = entityManager.find(Project.class, id);
  }

}

