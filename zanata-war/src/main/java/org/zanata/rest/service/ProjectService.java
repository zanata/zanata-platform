package org.zanata.rest.service;

import static org.zanata.common.EntityStatus.OBSOLETE;
import static org.zanata.common.EntityStatus.READONLY;

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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.enunciate.jaxrs.TypeHint;
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
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.validator.SlugValidator;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.base.Objects;

@Name("projectService")
@Path(ProjectService.SERVICE_PATH)
@Transactional
public class ProjectService implements ProjectResource
{

   public static final String PROJECT_SLUG_TEMPLATE = "{projectSlug:" + SlugValidator.PATTERN + "}";
   public static final String SERVICE_PATH = "/projects/p/" + PROJECT_SLUG_TEMPLATE;

   /** Project Identifier. */
   @PathParam("projectSlug")
   String projectSlug;

   @HeaderParam(HttpHeaderNames.ACCEPT)
   @DefaultValue(MediaType.APPLICATION_XML)
   @Context
   private MediaType accept;

   @Context
   private UriInfo uri;

   @HeaderParam("Content-Type")
   @Context
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

   /**
    * Returns header information for a project.
    * 
    * @return The following response status codes will be returned from this
    *         operation:<br>
    *         OK(200) - An "Etag" header for the requested project. <br>
    *         NOT FOUND(404) - If a project could not be found for the given
    *         parameters.<br>
    *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
    *         the server while performing this operation.
    */
   @Override
   @HEAD
   @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
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

   /**
    * Returns data for a single Project.
    * 
    * @return The following response status codes will be returned from this
    *         operation:<br>
    *         OK(200) - Containing the Project data.<br>
    *         NOT FOUND(404) - If a Project could not be found for the given
    *         parameters.<br>
    *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
    *         the server while performing this operation.
    */
   @Override
   @GET
   @Produces({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   @TypeHint(Project.class)
   public Response get()
   {
      try
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
      catch (NoSuchEntityException e)
      {
         return Response.status(Status.NOT_FOUND).build();
      }
   }

   /**
    * Creates or modifies a Project.
    * 
    * @param project The project's information.
    * @return The following response status codes will be returned from this
    *         method:<br>
    *         OK(200) - If an already existing project was updated as a result
    *         of this operation.<br>
    *         CREATED(201) - If a new project was added.<br>
    *         FORBIDDEN(403) - If the user was not allowed to create/modify the
    *         project. In this case an error message is contained in the
    *         response.<br>
    *         UNAUTHORIZED(401) - If the user does not have the proper
    *         permissions to perform this operation.<br>
    *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
    *         the server while performing this operation.
    */
   @Override
   @PUT
   @Consumes({ MediaTypes.APPLICATION_ZANATA_PROJECT_XML, MediaTypes.APPLICATION_ZANATA_PROJECT_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
   public Response put(Project project)
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
         hProject = new HProject();
         hProject.setSlug(projectSlug);
         // pre-emptive entity permission check
         identity.checkPermission(hProject, "insert");

         response = Response.created(uri.getAbsolutePath());
      }
      // Project is obsolete
      else if (Objects.equal(hProject.getStatus(), OBSOLETE))
      {
         return Response.status(Status.FORBIDDEN).entity("Project '" + projectSlug + "' is obsolete.").build();
      }
      // Project is ReadOnly
      else if (Objects.equal(hProject.getStatus(), READONLY))
      {
         return Response.status(Status.FORBIDDEN).entity("Project '" + projectSlug + "' is read-only.").build();
      }
      else
      {
         // must be an update operation
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

      if (project.getDefaultType() == null)
      {
         return Response.status(Status.BAD_REQUEST).entity("No valid default project type was specified.").build();
      }

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
      to.setDefaultProjectType(from.getDefaultType());
      // TODO Currently all Projects are created as Current
      // to.setStatus(from.getStatus());

      // keep source URLs unless they are specifically overwritten
      if (from.getSourceViewURL() != null)
      {
         to.setSourceViewURL(from.getSourceViewURL());
      }
      if (from.getSourceCheckoutURL() != null)
      {
         to.setSourceCheckoutURL(from.getSourceCheckoutURL());
      }
   }

   public static void transfer(HProject from, Project to)
   {
      to.setId(from.getSlug());
      to.setName(from.getName());
      to.setDescription(from.getDescription());
      to.setStatus(from.getStatus());
      to.setDefaultType(from.getDefaultProjectType());
      to.setSourceViewURL(from.getSourceViewURL());
      to.setSourceCheckoutURL(from.getSourceCheckoutURL());
   }

   public static Project toResource(HProject hProject, MediaType mediaType)
   {
      Project project = new Project();
      transfer(hProject, project);
      if (hProject instanceof HProject)
      {
         HProject itProject = (HProject) hProject;
         for (HProjectIteration pIt : itProject.getProjectIterations())
         {
            ProjectIteration iteration = new ProjectIteration();
            ProjectIterationService.transfer(pIt, iteration);
            iteration.getLinks(true).add(new Link(URI.create("iterations/i/" + pIt.getSlug()), "self", MediaTypes.createFormatSpecificType(MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION, mediaType)));
            project.getIterations(true).add(iteration);
         }
      }

      return project;
   }

}
