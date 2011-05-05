package org.zanata.rest.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.zanata.common.Namespaces;
import org.zanata.model.HProject;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectType;
import org.zanata.rest.service.ProjectsResource;

@Name("projectsService")
@Path("/projects")
@Transactional
public class ProjectsService implements ProjectsResource
{

   @In
   private Session session;

   @Logger
   Log log;

   @HeaderParam("Accept")
   @DefaultValue(MediaType.APPLICATION_XML)
   MediaType accept;

   @Override
   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECTS_XML, MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @Wrapped(element = "projects", namespace = Namespaces.ZANATA_API)
   public List<Project> get()
   {
      @SuppressWarnings("unchecked")
      List<HProject> projects = session.createQuery("from HProject p").list();

      List<Project> projectRefs = new ArrayList<Project>(projects.size());

      for (HProject hProject : projects)
      {
         Project project = new Project(hProject.getSlug(), hProject.getName(), ProjectType.IterationProject);
         project.getLinks(true).add(new Link(URI.create("p/" + hProject.getSlug()), "self", MediaTypes.createFormatSpecificType(MediaTypes.APPLICATION_ZANATA_PROJECT, accept)));
         projectRefs.add(project);
      }

      return projectRefs;
   }

}
