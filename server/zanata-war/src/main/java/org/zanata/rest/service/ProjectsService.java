/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
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
import org.zanata.rest.dto.resource.ResourceMeta;

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
   public Response get()
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

      Type genericType = new GenericType<List<Project>>()
      {
      }.getGenericType();
      Object entity = new GenericEntity<List<Project>>(projectRefs, genericType);
      
      return Response.ok(entity).build();
   }

}
