package org.fedorahosted.flies.core.rest;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.lang.String;

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
  
  @GET
  @Path("/{projectId}")
  @Produces("application/json")
  public Project getProject(@PathParam("projectId") long id) {
         return entityManager.find(Project.class, id);
  }

  @GET
  @Produces("application/json")
  public List<Project> getProjects() {
         Query q = entityManager.createQuery("select p from Project p");
         return q.getResultList(); 
  }
  

}

