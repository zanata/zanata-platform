package org.zanata.rest.editor.service;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.ETagUtils;
import org.zanata.rest.editor.service.resource.ProjectResource;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RequestScoped
@Named("editor.projectService")
@Path(ProjectResource.SERVICE_PATH)
@Transactional
public class ProjectService implements ProjectResource {
    @Context
    private Request request;
    @Inject
    private ETagUtils eTagUtils;
    @Inject
    private ProjectDAO projectDAO;
    @Inject
    private ValidationService validationService;

    @Override
    public Response getProject(@PathParam("projectSlug") String projectSlug) {
        try {
            EntityTag etag = eTagUtils.generateTagForProject(projectSlug);
            Response.ResponseBuilder response =
                    request.evaluatePreconditions(etag);
            if (response != null) {
                return response.build();
            }
            HProject hProject = projectDAO.getBySlug(projectSlug);
            Project project = org.zanata.rest.service.ProjectService
                    .toResource(hProject, MediaType.APPLICATION_JSON_TYPE);
            return Response.ok(project).tag(etag).build();
        } catch (NoSuchEntityException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @Override
    public Response getValidationSettings(
            @PathParam("projectSlug") String projectSlug,
            @PathParam("versionSlug") String versionSlug) {

        Collection<ValidationAction> validators =
            validationService.getValidationActions(projectSlug, versionSlug);

        if (validators == null || validators.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Map<ValidationId, ValidationAction.State> result =
                new HashMap<>();

        for (ValidationAction validationAction : validators) {
            result.put(validationAction.getId(), validationAction.getState());
        }
        return Response.ok(result).build();
    }

    public ProjectService() {
    }

    @java.beans.ConstructorProperties({ "request", "eTagUtils", "projectDAO",
            "validationService" })
    protected ProjectService(final Request request, final ETagUtils eTagUtils,
            final ProjectDAO projectDAO,
            final ValidationService validationService) {
        this.request = request;
        this.eTagUtils = eTagUtils;
        this.projectDAO = projectDAO;
        this.validationService = validationService;
    }
}
