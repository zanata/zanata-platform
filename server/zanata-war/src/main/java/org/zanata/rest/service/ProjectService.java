package org.zanata.rest.service;

import java.io.InputStream;
import java.net.URI;

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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;


import org.jboss.resteasy.util.HttpHeaderNames;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.validator.SlugValidator;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.service.ProjectResource;

@Name("projectService")
@Path(ProjectService.SERVICE_PATH)
@Transactional
public class ProjectService implements ProjectResource
{

   public static final String PROJECT_SLUG_TEMPLATE = "{projectSlug:" + SlugValidator.PATTERN + "}";
   public static final String SERVICE_PATH = "/projects/p/" + PROJECT_SLUG_TEMPLATE;

   @PathParam("projectSlug")
   String projectSlug;

   @HeaderParam(HttpHeaderNames.ACCEPT)
   @DefaultValue(MediaType.APPLICATION_XML)
   private MediaType accept;

   @Context
   private UriInfo uri;

   @HeaderParam("Content-Type")
   private MediaType requestContentType;

   @Context
   private HttpHeaders headers;

   @Context
   private Request request;

   Log log = Logging.getLog(ProjectService.class);

   @In
   ProjectDAO projectDAO;

   @In
   AccountDAO accountDAO;

   @In
   Identity identity;

   @In
   ETagUtils eTagUtils;

   public ProjectService()
   {
   }

   public ProjectService(ProjectDAO projectDAO, AccountDAO accountDAO, Identity identity, ETagUtils eTagUtils)
   {
      this.projectDAO = projectDAO;
      this.accountDAO = accountDAO;
      this.eTagUtils = eTagUtils;
      this.identity = identity;
   }

   @Override
   @HEAD
   @Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaTypes.APPLICATION_FLIES_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response head()
   {
      EntityTag etag = eTagUtils.generateTagForProject(projectSlug);
      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }
      return Response.ok().tag(etag).build();
   }

   @Override
   @GET
   @Produces( { MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaTypes.APPLICATION_FLIES_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response get()
   {
      EntityTag etag = eTagUtils.generateTagForProject(projectSlug);

      ResponseBuilder response = request.evaluatePreconditions(etag);
      if (response != null)
      {
         return response.build();
      }

      HProject hProject = projectDAO.getBySlug(projectSlug);

      Project project = toResource(hProject, accept);
      return Response.ok(project).tag(etag).build();
   }

   @Override
   @PUT
   @Consumes( { MediaTypes.APPLICATION_FLIES_PROJECT_XML, MediaTypes.APPLICATION_FLIES_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response put(InputStream messageBody)
   {

      ResponseBuilder response;
      EntityTag etag;

      HProject hProject = projectDAO.getBySlug(projectSlug);

      if (hProject == null)
      { // must be a create operation
         response = request.evaluatePreconditions();
         if (response != null)
         {
            return response.build();
         }
         hProject = new HIterationProject();
         hProject.setSlug(projectSlug);
         // pre-emptive entity permission check
         identity.checkPermission(hProject, "insert");

         response = Response.created(uri.getAbsolutePath());
      }
      else
      { // must be an update operation
         // pre-emptive entity permission check
         identity.checkPermission(hProject, "update");
         etag = eTagUtils.generateTagForProject(projectSlug);
         response = request.evaluatePreconditions(etag);
         if (response != null)
         {
            return response.build();
         }

         response = Response.ok();
      }

      Project project = RestUtils.unmarshall(Project.class, messageBody, requestContentType, headers.getRequestHeaders());
      transfer(project, hProject);

      hProject = projectDAO.makePersistent(hProject);
      projectDAO.flush();

      if (identity != null && hProject.getMaintainers().isEmpty())
      {
         HAccount hAccount = accountDAO.getByUsername(identity.getCredentials().getUsername());
         if (hAccount != null && hAccount.getPerson() != null)
         {
            hProject.getMaintainers().add(hAccount.getPerson());
         }
         projectDAO.flush();
      }

      etag = eTagUtils.generateTagForProject(projectSlug);
      return response.tag(etag).build();

   }

   public static void transfer(Project from, HProject to)
   {
      to.setName(from.getName());
      to.setDescription(from.getDescription());
   }

   public static void transfer(HProject from, Project to)
   {
      to.setId(from.getSlug());
      to.setName(from.getName());
      to.setDescription(from.getDescription());
   }

   public static Project toResource(HProject hProject, MediaType mediaType)
   {
      Project project = new Project();
      transfer(hProject, project);
      if (hProject instanceof HIterationProject)
      {
         HIterationProject itProject = (HIterationProject) hProject;
         for (HProjectIteration pIt : itProject.getProjectIterations())
         {
            ProjectIteration iteration = new ProjectIteration();
            ProjectIterationService.transfer(pIt, iteration);
            iteration.getLinks(true).add(new Link(URI.create("iterations/i/" + pIt.getSlug()), "self", MediaTypes.createFormatSpecificType(MediaTypes.APPLICATION_FLIES_PROJECT_ITERATION, mediaType)));
            project.getIterations(true).add(iteration);
         }
      }

      return project;
   }

}
