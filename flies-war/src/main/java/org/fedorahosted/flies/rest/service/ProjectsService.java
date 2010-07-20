package org.fedorahosted.flies.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.model.HProject;
import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.Link;
import org.fedorahosted.flies.rest.dto.Project;
import org.fedorahosted.flies.rest.dto.ProjectType;
import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;

@Name("projectsService")
@Path("/projects")
public class ProjectsService
{

   @In
   Session session;

   @Logger
   Log log;

   @HeaderParam("Accept")
   @DefaultValue(MediaType.APPLICATION_XML)
   MediaType accept;

   @GET
   @Produces( { MediaTypes.APPLICATION_FLIES_PROJECTS_XML, MediaTypes.APPLICATION_FLIES_PROJECTS_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @Wrapped(element = "projects", namespace = Namespaces.FLIES)
   public List<Project> get()
   {
      List<HProject> projects = session.createQuery("from HProject p").list();

      List<Project> projectRefs = new ArrayList<Project>(projects.size());

      for (HProject hProject : projects)
      {
         Project project = new Project(hProject.getSlug(), hProject.getName(), ProjectType.IterationProject);
         project.getLinks(true).add(new Link(URI.create("p/" + hProject.getSlug()), "self", MediaTypes.createFormatSpecificType(MediaTypes.APPLICATION_FLIES_PROJECT, accept)));
         projectRefs.add(project);
      }

      return projectRefs;
   }

}
