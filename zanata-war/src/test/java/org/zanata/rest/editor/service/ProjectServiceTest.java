package org.zanata.rest.editor.service;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.ETagUtils;

import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@Test(groups = "unit-tests")
public class ProjectServiceTest {
    private ProjectService service;
    @Mock
    private ProjectDAO projectDAO;
    @Mock
    private Request request;
    @Mock
    private ETagUtils etagUtil;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new ProjectService(request, etagUtil, projectDAO);
    }

    @Test
    public void willReturnIfDoesNotMeetPreconditions() {
        EntityTag entityTag = new EntityTag("1");
        when(etagUtil.generateTagForProject("about-fedora")).thenReturn(
                entityTag);
        when(request.evaluatePreconditions(entityTag)).thenReturn(
                Response.status(
                        Response.Status.BAD_REQUEST));
        Response project = service.getProject("about-fedora");

        Assertions.assertThat(project.getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void willReturnFoundProject() {
        when(projectDAO.getBySlug("about-fedora")).thenReturn(new HProject());

        Response response = service.getProject("about-fedora");
        Assertions.assertThat(response.getStatus()).isEqualTo(200);
        Assertions.assertThat(response.getEntity()).isInstanceOf(Project.class);
    }

    @Test
    public void willReturnNotFoundIfSlugNotFound() {
        when(projectDAO.getBySlug("about-fedora"))
                .thenThrow(new NoSuchEntityException());

        Response response = service.getProject("about-fedora");
        Assertions.assertThat(response.getStatus()).isEqualTo(404);
    }
}
