package org.zanata.rest.editor.service;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.dao.ProjectDAO;
import org.zanata.model.HProject;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.dto.Project;
import org.zanata.rest.service.ETagUtils;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.resources.ValidationMessages;
import org.zanata.webtrans.shared.validation.ValidationFactory;

import java.util.Collection;
import java.util.HashMap;

import static org.mockito.Mockito.when;

public class ProjectServiceTest {
    private ProjectService service;
    @Mock
    private ProjectDAO projectDAO;
    @Mock
    private Request request;
    @Mock
    private ETagUtils etagUtil;
    @Mock
    private ValidationService validationService;

    private ValidationFactory validationFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service = new ProjectService(
                request, etagUtil, projectDAO,validationService);
        ValidationMessages message =
                Gwti18nReader.create(ValidationMessages.class);
        validationFactory = new ValidationFactory(message);
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
    public void willReturnNotFoundIfValidatorsNotFound() {
        when(validationService.getValidationActions("about-fedora", "ver1"))
                .thenReturn(null);
        Response response = service.getValidationSettings(
                "about-fedora", "ver1");
        Assertions.assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void willReturnFoundValidators() {
        Collection<ValidationAction> validationList =
                validationFactory.getAllValidationActions().values();
        when(validationService.getValidationActions("about-fedora", "ver1"))
                .thenReturn(validationList);
        Response response = service.getValidationSettings(
                "about-fedora", "ver1");
        // Modify the validationList result identically
        HashMap<ValidationId, ValidationAction.State> result =
                new HashMap<ValidationId, ValidationAction.State>();
        for (ValidationAction validationAction : validationList) {
            result.put(validationAction.getId(), validationAction.getState());
        }
        Assertions.assertThat(response.getStatus()).isEqualTo(200);
        Assertions.assertThat(response.getEntity())
                .isEqualToComparingFieldByField(result);
    }

    @Test
    public void willReturnNotFoundIfSlugNotFound() {
        when(projectDAO.getBySlug("about-fedora"))
                .thenThrow(new NoSuchEntityException());

        Response response = service.getProject("about-fedora");
        Assertions.assertThat(response.getStatus()).isEqualTo(404);
    }
}
