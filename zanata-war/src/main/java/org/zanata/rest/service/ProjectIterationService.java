/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.rest.service;

import static org.zanata.common.EntityStatus.OBSOLETE;
import static org.zanata.common.EntityStatus.READONLY;

import javax.annotation.Nonnull;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.Identity;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.validator.SlugValidator;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.seam.resteasy.IgnoreInterfacePath;

import com.google.common.base.Objects;

@Name("projectIterationService")
//@Path(ProjectIterationService.SERVICE_PATH)
@Transactional
@IgnoreInterfacePath
public class ProjectIterationService implements ProjectIterationResource
{
   /** Project Identifier. */
   @PathParam("projectSlug")
   private String projectSlug;

   /** Project Iteration identifier. */
   @PathParam("iterationSlug")
   private String iterationSlug;

   @HeaderParam("Content-Type")
   @Context
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @HeaderParam(HttpHeaderNames.ACCEPT)
   @DefaultValue(MediaType.APPLICATION_XML)
   @Context
   private MediaType accept;

   @Context
   private UriInfo uri;

   @Context
   private Request request;

   @In
   ProjectDAO projectDAO;

   @In
   ProjectIterationDAO projectIterationDAO;

   @In
   ETagUtils eTagUtils;

   @In
   Identity identity;

   @SuppressWarnings("null")
   @Nonnull
   public String getProjectSlug()
   {
      return projectSlug;
   }

   @SuppressWarnings("null")
   @Nonnull
   public String getIterationSlug()
   {
      return iterationSlug;
   }

   @Override
   public Response head()
   {
      EntityTag etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      return Response.ok().tag(etag).build();
   }

   @Override
   public Response get()
   {

      EntityTag etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(getProjectSlug(), getIterationSlug());

      ProjectIteration it = new ProjectIteration();
      transfer(hProjectIteration, it);

      return Response.ok(it).tag(etag).build();
   }

   @Override
   public Response put(ProjectIteration projectIteration)
   {

      ResponseBuilder response;
      EntityTag etag = null;
      boolean changed = false;

      Response projTypeError = getProjectTypeError(projectIteration.getProjectType());
      if (projTypeError != null)
      {
         return projTypeError;
      }

      HProject hProject = projectDAO.getBySlug(getProjectSlug());

      if (hProject == null)
      {
         return Response.status(Status.NOT_FOUND).entity("Project '" + projectSlug + "' not found.").build();
      }
      // Project is Obsolete
      else if(Objects.equal(hProject.getStatus(), OBSOLETE))
      {
         return Response.status(Status.NOT_FOUND).entity("Project '" + projectSlug + "' not found.").build();
      }
      // Project is ReadOnly
      else if(Objects.equal(hProject.getStatus(), READONLY))
      {
         return Response.status(Status.FORBIDDEN).entity("Project '" + projectSlug + "' is read-only.").build();
      }

      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(getProjectSlug(), getIterationSlug());

      if (hProjectIteration == null)
      { // must be a create operation
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }

         hProjectIteration = new HProjectIteration();
         hProjectIteration.setSlug(iterationSlug);

         copyProjectConfiguration(projectIteration, hProjectIteration, hProject);

         hProject.addIteration(hProjectIteration);
         // pre-emptive entity permission check
         // identity.checkPermission(hProject, "add-iteration");
         identity.checkPermission(hProjectIteration, "insert");

         response = Response.created(uri.getAbsolutePath());
         changed = true;
      }
      // Iteration is Obsolete
      else if(Objects.equal(hProjectIteration.getStatus(), OBSOLETE))
      {
         return Response.status(Status.FORBIDDEN).entity("Project Iiteration '" + projectSlug + ":" + iterationSlug + "' is obsolete.").build();
      }
      // Iteration is ReadOnly
      else if(Objects.equal(hProjectIteration.getStatus(), READONLY))
      {
         return Response.status(Status.FORBIDDEN).entity("Project Iteration '" + projectSlug + ":" + iterationSlug + "' is read-only.").build();
      }
      else
      { // must be an update operation
         // pre-emptive entity permission check
         identity.checkPermission(hProjectIteration, "update");

         copyProjectConfiguration(projectIteration, hProjectIteration, null);

         etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);
         response = request.evaluatePreconditions(etag);
         if (response != null)
         {
            return response.build();
         }
         response = Response.ok();
         changed = true;
      }

      if (changed)
      {
         projectIterationDAO.makePersistent(hProjectIteration);
         projectIterationDAO.flush();
         etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);
      }
      return response.tag(etag).build();

   }

   /**
    * Generate an error response object for invalid projectType.
    * Valid types are null or a type that is valid for this server.
    * 
    * @param projectType to check
    * @return null if projectType is valid, otherwise a Response object with appropriate error string
    */
   private Response getProjectTypeError(String projectType)
   {
      if (projectType != null)
      {
         if (projectType.isEmpty())
         {
            return Response.status(Status.BAD_REQUEST)
                  .entity("If a project type is specified, it must not be empty.")
                  .build();
         }

         try
         {
            ProjectType.getValueOf(projectType);
         }
         catch (Exception e)
         {
            String validTypes = StringUtils.join(ProjectType.values(), ", ");
            return Response.status(Status.BAD_REQUEST)
                  .entity("Project type \"" + projectType + "\" not valid for this server." +
                        " Valid types: [" + validTypes + "]")
                        .build();
         }
      }
      return null;
   }

   /**
    * Copy project configuration into new version(project type and validation
    * rules)
    * 
    * @param from must have pre-validated project type
    * @param to
    * @param hProject
    */
   public static void copyProjectConfiguration(ProjectIteration from, HProjectIteration to, HProject hProject)
   {
      ProjectType projectType;
      try
      {
         projectType = ProjectType.getValueOf(from.getProjectType());
      }
      catch (Exception e)
      {
         projectType = null;
      }

      if (projectType == null)
      {
         if(hProject != null)
         {
            to.setProjectType(hProject.getDefaultProjectType());
         }
      }
      else
      {
         to.setProjectType(projectType);
      }

      if (hProject != null)
      {
         to.setOverrideValidations(hProject.getOverrideValidations());
         to.getCustomizedValidations().addAll(hProject.getCustomizedValidations());
      }
   }

   public static void transfer(HProjectIteration from, ProjectIteration to)
   {
      to.setId(from.getSlug());
      to.setStatus(from.getStatus());
      if (from.getProjectType() != null)
      {
         to.setProjectType(from.getProjectType().toString());
      }
   }

}
