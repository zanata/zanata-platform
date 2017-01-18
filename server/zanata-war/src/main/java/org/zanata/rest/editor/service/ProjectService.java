package org.zanata.rest.editor.service;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Project;
import org.zanata.rest.editor.service.resource.ProjectResource;
import org.zanata.rest.service.ETagUtils;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

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

    @java.beans.ConstructorProperties({ "request", "eTagUtils", "projectDAO" })
    protected ProjectService(Request request, ETagUtils eTagUtils,
            ProjectDAO projectDAO) {
        this.request = request;
        this.eTagUtils = eTagUtils;
        this.projectDAO = projectDAO;
    }

    public ProjectService() {
    }

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
            Project project =
                    org.zanata.rest.service.ProjectService.toResource(hProject,
                            MediaType.APPLICATION_JSON_TYPE);
            return Response.ok(project).tag(etag).build();
        } catch (NoSuchEntityException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
