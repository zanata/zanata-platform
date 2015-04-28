package org.zanata.rest.editor.service;

import java.util.List;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.hibernate.transform.ResultTransformer;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.rest.ReadOnlyEntityException;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.editor.dto.Locale;
import org.zanata.rest.service.ETagUtils;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.search.FilterConstraints;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.server.rpc.GetTransUnitsNavigationService;
import org.zanata.webtrans.shared.model.DocumentId;

import com.google.common.collect.ImmutableMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@Test(groups = "unit-tests")
public class ProjectVersionServiceTest {
    private ProjectVersionService service;
    @Mock
    private Request request;
    @Mock
    private ETagUtils etagUtil;
    @Mock
    private TextFlowDAO textFlowDAO;
    @Mock
    private DocumentDAO documentDAO;
    @Mock
    private ProjectIterationDAO projectIterationDAO;
    @Mock
    private LocaleService localeService;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        service =
                new ProjectVersionService(textFlowDAO, documentDAO,
                        projectIterationDAO, localeService, request, etagUtil,
                        new ResourceUtils());

    }

    @Test
    public void getVersionWillReturnIfDoesNotMeetPreconditions() {
        EntityTag entityTag = new EntityTag("1");
        when(etagUtil.generateETagForIteration("about-fedora", "master"))
                .thenReturn(
                        entityTag);
        when(request.evaluatePreconditions(entityTag)).thenReturn(
                Response.status(
                        Response.Status.BAD_REQUEST));
        Response response = service.getVersion("about-fedora", "master");

        assertThat(response.getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void willReturnFoundProject() {
        when(projectIterationDAO.getBySlug("about-fedora", "master"))
                .thenReturn(new HProjectIteration());

        Response response = service.getVersion("about-fedora", "master");
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(
                ProjectIteration.class);
    }

    @Test
    public void willReturnNotFoundIfSlugNotFound() {
        when(projectIterationDAO.getBySlug("about-fedora", "master"))
                .thenReturn(null);

        Response response = service.getVersion("about-fedora", "master");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getLocalesWillReturnNotFoundIfVersionNotFound() {
        when(projectIterationDAO.getBySlug("about-fedora", "master"))
                .thenReturn(null);

        Response response = service.getLocales("about-fedora", "master");

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getLocalesWillReturnLocales() {
        when(projectIterationDAO.getBySlug("about-fedora", "master"))
                .thenReturn(new HProjectIteration());
        when(
                localeService.getSupportedLanguageByProjectIteration(
                        "about-fedora", "master")).thenReturn(
                Lists.newArrayList(new HLocale(LocaleId.DE), new HLocale(
                        LocaleId.ES)));

        Response response = service.getLocales("about-fedora", "master");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(
                GenericEntity.class);
        List<Locale> iterationList =
                ((GenericEntity<List<Locale>>) response.getEntity())
                        .getEntity();
        assertThat(iterationList).extracting("localeId")
                .contains(LocaleId.DE, LocaleId.ES);
    }

    @Test(expectedExceptions = NoSuchEntityException.class)
    public void willThrowNoSuchEntityExceptionIfVersionNotFound() {
        when(projectIterationDAO.getBySlug("a", "1")).thenReturn(null);
        service.retrieveAndCheckIteration("a", "1", false);
    }

    @Test(expectedExceptions = NoSuchEntityException.class)
    public void willThrowNoSuchEntityExceptionIfVersionIsObsolete() {
        HProjectIteration iteration = new HProjectIteration();
        iteration.setStatus(EntityStatus.OBSOLETE);
        when(projectIterationDAO.getBySlug("a", "1")).thenReturn(iteration);
        service.retrieveAndCheckIteration("a", "1", false);
    }

    @Test(expectedExceptions = NoSuchEntityException.class)
    public void willThrowNoSuchEntityExceptionIfProjectIsObsolete() {
        HProjectIteration iteration = new HProjectIteration();
        HProject project = new HProject();
        iteration.setProject(project);
        project.setStatus(EntityStatus.OBSOLETE);
        when(projectIterationDAO.getBySlug("a", "1")).thenReturn(iteration);
        service.retrieveAndCheckIteration("a", "1", false);
    }

    @Test(expectedExceptions = ReadOnlyEntityException.class)
    public
            void
            willThrowReadOnlyEntityExceptionIfVersionIsReadOnlyButRequireWritePermission() {
        HProjectIteration iteration = new HProjectIteration();
        iteration.setProject(new HProject());
        iteration.setStatus(EntityStatus.READONLY);
        when(projectIterationDAO.getBySlug("a", "1")).thenReturn(iteration);
        service.retrieveAndCheckIteration("a", "1", true);
    }

    @Test(expectedExceptions = ReadOnlyEntityException.class)
    public
            void
            willThrowReadOnlyEntityExceptionIfProjectIsReadOnlyButRequireWritePermission() {
        HProjectIteration iteration = new HProjectIteration();
        HProject project = new HProject();
        iteration.setProject(project);
        project.setStatus(EntityStatus.READONLY);
        when(projectIterationDAO.getBySlug("a", "1")).thenReturn(iteration);
        service.retrieveAndCheckIteration("a", "1", true);
    }

    @Test
    public void willReturnProjectIterationIfAllGood() {
        HProjectIteration iteration = new HProjectIteration();
        HProject project = new HProject();
        iteration.setProject(project);
        when(projectIterationDAO.getBySlug("a", "1")).thenReturn(iteration);
        HProjectIteration result =
                service.retrieveAndCheckIteration("a", "1", false);

        assertThat(result).isSameAs(iteration);
    }

    @Test
    public void getDocumentWillReturnIfDoesNotMeetPreconditions() {
        HProjectIteration iteration = new HProjectIteration();
        HProject project = new HProject();
        iteration.setProject(project);
        when(projectIterationDAO.getBySlug("about-fedora", "master"))
                .thenReturn(iteration);
        EntityTag entityTag = new EntityTag("1");
        when(projectIterationDAO.getResourcesETag(iteration))
                .thenReturn(
                        entityTag);
        when(request.evaluatePreconditions(entityTag)).thenReturn(
                Response.status(
                        Response.Status.BAD_REQUEST));
        Response response = service.getDocuments("about-fedora", "master");

        assertThat(response.getStatus()).isEqualTo(
                Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void getDocumentWillReturnDocumentMetaList() {
        HProjectIteration iteration = new HProjectIteration();
        HProject project = new HProject();
        iteration.setProject(project);
        iteration.setDocuments(ImmutableMap.<String, HDocument> builder()
                .put("a", new HDocument("pot/authors", ContentType.PO,
                        new HLocale(LocaleId.EN_US))).build());
        when(projectIterationDAO.getBySlug("about-fedora", "master"))
                .thenReturn(iteration);

        Response response = service.getDocuments("about-fedora", "master");

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isInstanceOf(GenericEntity.class);
        List<ResourceMeta> docList =
                ((GenericEntity<List<ResourceMeta>>) response.getEntity())
                        .getEntity();
        assertThat(docList).extracting("name").contains("pot/authors");
    }

    @Test
    public void getTransUnitStatusWillReturnNotFoundIfDocumentNotFound() {
        when(documentDAO.getByProjectIterationAndDocId("a", "1", "authors"))
                .thenReturn(null);

        Response response =
                service.getTransUnitStatus("a", "1", "authors", "de");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getTransUnitStatusWillReturnNotFoundIfLocaletNotFound() {
        when(documentDAO.getByProjectIterationAndDocId("a", "1", "authors"))
                .thenReturn(new HDocument());
        when(localeService.getByLocaleId("de")).thenReturn(null);

        Response response =
                service.getTransUnitStatus("a", "1", "authors", "de");
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void getTransUnitStatusWillGetResults() {
        when(documentDAO.getByProjectIterationAndDocId("a", "1", "authors"))
                .thenReturn(new HDocument());
        when(localeService.getByLocaleId("de")).thenReturn(
                new HLocale(LocaleId.DE));

        Response response =
                service.getTransUnitStatus("a", "1", "authors", "de");
        assertThat(response.getStatus()).isEqualTo(200);
        verify(textFlowDAO).getNavigationByDocumentId(isA(DocumentId.class), isA(HLocale.class), isA(
                GetTransUnitsNavigationService.TextFlowResultTransformer.class), isA(FilterConstraints.class));
    }
}
