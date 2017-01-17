package org.zanata.rest.service;

import static org.zanata.common.EntityStatus.ACTIVE;
import static org.zanata.common.EntityStatus.OBSOLETE;
import static org.zanata.common.EntityStatus.READONLY;
import static org.zanata.model.ProjectRole.Maintainer;
import static org.zanata.rest.service.GlossaryService.PROJECT_QUALIFIER_PREFIX;

import java.net.URI;

import javax.annotation.Nonnull;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.util.HttpHeaderNames;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.common.ProjectType;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.QualifiedName;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.WebhookServiceImpl;
import org.zanata.util.GlossaryUtil;
import org.zanata.webhook.events.ProjectMaintainerChangedEvent;

import com.google.common.base.Objects;

@RequestScoped
@Named("projectService")
@Path(ProjectResource.SERVICE_PATH)
@Transactional
public class ProjectService implements ProjectResource {
    /** Project Identifier. */
    @PathParam("projectSlug")
    String projectSlug;

    @HeaderParam(HttpHeaderNames.ACCEPT)
    @DefaultValue(MediaType.APPLICATION_XML)
    @Context
    private MediaType accept;

    @Context
    private UriInfo uri;
    @Context
    private Request request;

    @Inject
    ProjectDAO projectDAO;

    @Inject
    AccountDAO accountDAO;

    @Inject
    ZanataIdentity identity;

    @Inject
    WebhookServiceImpl webhookServiceImpl;

    @Inject
    ETagUtils eTagUtils;

    @SuppressWarnings("null")
    @Nonnull
    public String getProjectSlug() {
        return projectSlug;
    }

    @Override
    public Response head() {
        EntityTag etag = eTagUtils.generateTagForProject(projectSlug);
        ResponseBuilder response = request.evaluatePreconditions(etag);
        if (response != null) {
            return response.build();
        }
        return Response.ok().tag(etag).build();
    }

    @Override
    public Response get() {
        try {
            EntityTag etag = eTagUtils.generateTagForProject(getProjectSlug());

            ResponseBuilder response = request.evaluatePreconditions(etag);
            if (response != null) {
                return response.build();
            }

            HProject hProject = projectDAO.getBySlug(getProjectSlug());
            Project project = toResource(hProject, accept);
            return Response.ok(project).tag(etag).build();
        } catch (NoSuchEntityException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response put(Project project) {
        ResponseBuilder response;
        EntityTag etag;

        HProject hProject = projectDAO.getBySlug(getProjectSlug());

        if (hProject == null) { // must be a create operation
            response = request.evaluatePreconditions();
            if (response != null) {
                return response.build();
            }
            hProject = new HProject();
            hProject.setSlug(projectSlug);
            // pre-emptive entity permission check
            identity.checkPermission(hProject, "insert");

            response = Response.created(uri.getAbsolutePath());
        } else if (Objects.equal(hProject.getStatus(), OBSOLETE)) {
            // Project is obsolete
            return Response.status(Status.FORBIDDEN)
                    .entity("Project '" + projectSlug + "' is obsolete.")
                    .build();
        } else {
            // must be an update operation
            // pre-emptive entity permission check
            identity.checkPermission(hProject, "update");
            etag = eTagUtils.generateTagForProject(projectSlug);
            response = request.evaluatePreconditions(etag);
            if (response != null) {
                return response.build();
            }

            response = Response.ok();
        }

        if (Objects.equal(hProject.getStatus(), READONLY)
                && !Objects.equal(project.getStatus(), ACTIVE)) {
            // User attempting to update a ReadOnly project
            return Response.status(Status.FORBIDDEN)
                    .entity("Project '" + projectSlug + "' is read-only.")
                    .build();
        }

        // null project type accepted for compatibility with old clients
        if (project.getDefaultType() != null) {
            if (project.getDefaultType().isEmpty()) {
                return Response.status(Status.BAD_REQUEST)
                        .entity("No valid default project type was specified.")
                        .build();
            }

            try {
                ProjectType.getValueOf(project.getDefaultType());
            } catch (Exception e) {
                String validTypes =
                        StringUtils.join(ProjectType.values(), ", ");
                return Response
                        .status(Status.BAD_REQUEST)
                        .entity("Project type \"" + project.getDefaultType()
                                + "\" not valid for this server."
                                + " Valid types: [" + validTypes + "]").build();
            }
        }

        updateProject(project, hProject);

        if (identity != null && hProject.getMaintainers().isEmpty()) {
            HAccount hAccount =
                    accountDAO.getByUsername(identity.getCredentials()
                            .getUsername());
            if (hAccount != null && hAccount.getPerson() != null) {
                hProject.addMaintainer(hAccount.getPerson());
            }
        }

        projectDAO.makePersistent(hProject);
        projectDAO.flush();
        etag = eTagUtils.generateTagForProject(projectSlug);

        webhookServiceImpl.processWebhookMaintainerChanged(
                hProject.getSlug(),
                identity.getCredentials().getUsername(),
                Maintainer, hProject.getWebHooks(),
                ProjectMaintainerChangedEvent.ChangeType.ADD);
        return response.tag(etag).build();

    }

    @Override
    public Response getGlossaryQualifiedName() {
        try {
            EntityTag etag = eTagUtils.generateTagForProject(getProjectSlug());
            ResponseBuilder response = request.evaluatePreconditions(etag);
            if (response != null) {
                return response.build();
            }

            QualifiedName qualifiedName =
                    new QualifiedName(getGlossaryQualifiedName(projectSlug));
            return Response.ok(qualifiedName).tag(etag).build();
        } catch (NoSuchEntityException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    public static String getGlossaryQualifiedName(String projectSlug) {
        return GlossaryUtil.generateQualifiedName(PROJECT_QUALIFIER_PREFIX,
                projectSlug);
    }

    private static void updateProject(Project from, HProject to) {
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        if (from.getDefaultType() != null) {
            ProjectType projectType;
            try {
                projectType = ProjectType.getValueOf(from.getDefaultType());
            } catch (Exception e) {
                projectType = null;
            }

            if (projectType != null) {
                to.setDefaultProjectType(projectType);
            }
        }
        if (from.getStatus() != null) {
            to.setStatus(from.getStatus());
        }

        // keep source URLs unless they are specifically overwritten
        if (from.getSourceViewURL() != null) {
            to.setSourceViewURL(from.getSourceViewURL());
        }
        if (from.getSourceCheckoutURL() != null) {
            to.setSourceCheckoutURL(from.getSourceCheckoutURL());
        }
    }

    private static void getProjectDetails(HProject from, Project to) {
        to.setId(from.getSlug());
        to.setName(from.getName());
        to.setDescription(from.getDescription());
        to.setStatus(from.getStatus());
        if (from.getDefaultProjectType() != null) {
            to.setDefaultType(from.getDefaultProjectType().toString());
        }
        to.setSourceViewURL(from.getSourceViewURL());
        to.setSourceCheckoutURL(from.getSourceCheckoutURL());
    }

    public static Project toResource(HProject hProject, MediaType mediaType) {
        Project project = new Project();
        getProjectDetails(hProject, project);
        for (HProjectIteration pIt : hProject.getProjectIterations()) {
            ProjectIteration iteration = new ProjectIteration();
            ProjectVersionService.getProjectVersionDetails(pIt, iteration);

            iteration
                    .getLinks(true)
                    .add(new Link(
                            URI.create("iterations/i/" + pIt.getSlug()),
                            "self",
                            MediaTypes
                                    .createFormatSpecificType(
                                            MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION,
                                            mediaType)));
            project.getIterations(true).add(iteration);
        }
        return project;
    }

}
