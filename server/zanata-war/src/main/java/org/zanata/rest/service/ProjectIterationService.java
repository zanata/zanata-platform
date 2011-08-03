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

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.security.Identity;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.validator.SlugValidator;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.ProjectIteration;

@Name("projectIterationService")
@Path(ProjectIterationService.SERVICE_PATH)
@Transactional
public class ProjectIterationService implements ProjectIterationResource
{

   public static final String ITERATION_SLUG_TEMPLATE = "{iterationSlug:" + SlugValidator.PATTERN + "}";
   public static final String SERVICE_PATH = ProjectService.SERVICE_PATH + "/iterations/i/" + ITERATION_SLUG_TEMPLATE;

   @PathParam("projectSlug")
   private String projectSlug;

   @PathParam("iterationSlug")
   private String iterationSlug;

   @HeaderParam("Content-Type")
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @HeaderParam(HttpHeaderNames.ACCEPT)
   @DefaultValue(MediaType.APPLICATION_XML)
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

   public ProjectIterationService()
   {
   }

   public ProjectIterationService(ProjectDAO projectDAO, ProjectIterationDAO projectIterationDAO, Identity identity, ETagUtils eTagUtils)
   {
      this.projectDAO = projectDAO;
      this.projectIterationDAO = projectIterationDAO;
      this.identity = identity;
      this.eTagUtils = eTagUtils;
   }

   @Override
   @HEAD
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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
   @GET
   @Produces( { MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response get()
   {

      EntityTag etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      ProjectIteration it = new ProjectIteration();
      transfer(hProjectIteration, it);

      return Response.ok(it).tag(etag).build();
   }

   @Override
   @PUT
   @Consumes( { MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response put(InputStream messageBody)
   {

      ResponseBuilder response;
      EntityTag etag = null;
      boolean changed = false;

      HProject hProject = projectDAO.getBySlug(projectSlug);

      if (hProject == null)
      {
         return Response.status(Status.NOT_FOUND).entity("Project '" + projectSlug + "' not found.").build();
      }

      HProjectIteration hProjectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);

      if (hProjectIteration == null)
      { // must be a create operation
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }
         hProjectIteration = new HProjectIteration();
         hProjectIteration.setSlug(iterationSlug);
         hProjectIteration.setProject((HIterationProject) hProject);
         // pre-emptive entity permission check
         // identity.checkPermission(hProject, "add-iteration");
         identity.checkPermission(hProjectIteration, "insert");

         response = Response.created(uri.getAbsolutePath());
         changed = true;
      }
      else
      { // must be an update operation
         // pre-emptive entity permission check
         identity.checkPermission(hProjectIteration, "update");
         etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);
         response = request.evaluatePreconditions(etag);
         if (response != null)
         {
            return response.build();
         }
         response = Response.ok();
      }


      if (changed)
      {
         projectIterationDAO.makePersistent(hProjectIteration);
         projectIterationDAO.flush();
         etag = eTagUtils.generateETagForIteration(projectSlug, iterationSlug);
      }
      return response.tag(etag).build();

   }


   public static void transfer(HProjectIteration from, ProjectIteration to)
   {
      to.setId(from.getSlug());
   }

}
