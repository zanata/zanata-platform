package org.zanata.webtrans.server;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.assertj.core.matcher.AssertionMatcher;
import org.jglue.cdiunit.ProducerConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.service.ValidationService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;
import org.zanata.webtrans.test.GWTTestData;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.ArrayList;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.zanata.webtrans.server.TranslationWorkspaceManagerImplTest.UseRealWorkspaceManagerFactory;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@UseRealWorkspaceManagerFactory(true)
public class TranslationWorkspaceManagerImplTest {

    @Retention(RUNTIME)
    @Target({METHOD, TYPE})
    @ProducerConfig
    public @interface UseRealWorkspaceManagerFactory {
        boolean value();
    }

    static HProjectIteration makeHProjectIteration(
            String projectSlugAndName, String iterationSlug) {
        HProjectIteration projectIteration = new HProjectIteration();
        HProject project = new HProject();
        project.setSlug(projectSlugAndName);
        project.setName(projectSlugAndName);
        projectIteration.setProject(project);
        projectIteration.setSlug(iterationSlug);
        return projectIteration;
    }

    @Inject
    private TranslationWorkspaceManagerImpl manager;

    @Produces @Mock
    private AccountDAO accountDAO;
    @Produces @Mock
    private GravatarService gravatarServiceImpl;
    @Produces @Mock
    private ProjectIterationDAO projectIterationDAO;
    @Produces @Mock
    private LocaleService localeServiceImpl;
    @Produces @Mock
    private ValidationService validationServiceImpl;
    @Produces @Mock
    private TranslationWorkspace mockWorkspace;
    @Captor
    private ArgumentCaptor<ExitWorkspace> eventCaptor;
    private Optional<String> oldProjectSlug = Optional.absent();
    private Optional<String> oldIterationSlug = Optional.absent();
    @Produces @Mock
    private EntityManager entityManager;

    // only used by some tests:
    @Mock
    private TranslationWorkspaceFactory mockWorkspaceFactory;
    @Mock
    private TranslationWorkspace mockWorkspaceMaster;
    @Mock
    private TranslationWorkspace mockWorkspace1;
    @Mock
    private TranslationWorkspace mockWorkspace2;


    @Produces
    private TranslationWorkspaceFactory getTranslationWorkspaceFactory(UseRealWorkspaceManagerFactory config) {
        if (config.value()) {
            return new TranslationWorkspaceFactory(projectIterationDAO, localeServiceImpl);
        } else {
            return mockWorkspaceFactory;
        }
    }

    @Test(expected = NoSuchWorkspaceException.class)
    public void testRegisterInvalidWorkspace() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(null);

        manager.getOrRegisterWorkspace(workspaceId);
    }

    @Test(expected = NoSuchWorkspaceException.class)
    public void testRegisterWorkspaceForObsoleteProjectIteration()
            throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        HProjectIteration projectIteration = makeHProjectIteration("a", "1");
        projectIteration.getProject().setStatus(EntityStatus.OBSOLETE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(projectIteration);

        manager.getOrRegisterWorkspace(workspaceId);
    }

    @Test(expected = NoSuchWorkspaceException.class)
    public void testRegisterWorkspaceWithInvalidLocale() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(makeHProjectIteration("a", "1"));
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(null);

        manager.getOrRegisterWorkspace(workspaceId);
    }

    @Test(expected = NoSuchWorkspaceException.class)
    public void testRegisterWorkspaceWithInactiveLocale() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(makeHProjectIteration("project", "1"));
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(false);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);

        manager.getOrRegisterWorkspace(workspaceId);
    }

    @Test
    public void testRegisterNewWorkspace() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(makeHProjectIteration("project", "master"));
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);

        TranslationWorkspace workspace =
                manager.getOrRegisterWorkspace(workspaceId);

        WorkspaceContext context = workspace.getWorkspaceContext();
        assertThat(context.getWorkspaceId()).isEqualTo(workspaceId);
        assertThat(context.getLocaleName()).isEqualTo("German");
        assertThat(context.getWorkspaceName()).isEqualTo("project (master)");
    }

    @Test
    public void testGetRegisteredNewWorkspace() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(makeHProjectIteration("a", "1"));
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);

        TranslationWorkspace workspace =
                manager.getOrRegisterWorkspace(workspaceId);

        // call again with same workspace id will return same instance
        TranslationWorkspace anotherWorkspace =
                manager.getOrRegisterWorkspace(workspaceId);
        assertThat(anotherWorkspace).isSameAs(workspace);
    }

    @Test
    public void testExitWorkspaceWithNullSessionId() throws Exception {
        TranslationWorkspaceManagerImpl spy = spy(manager);

        spy.exitWorkspace("admin", null, "Administrator", "admin@example.com");

        verifyZeroInteractions(accountDAO);
    }

    @Test
    @UseRealWorkspaceManagerFactory(false)
    public void testExitWorkspace() throws Exception {
        HProjectIteration projectIteration =
                makeHProjectIteration("project", "master");
        WorkspaceId workspaceId = GWTTestData.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                                .getProjectIterationId().getProjectSlug(),
                        workspaceId
                                .getProjectIterationId()
                                .getIterationSlug()))
                .thenReturn(projectIteration);
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);
        TranslationWorkspaceManagerImpl spy = spy(manager);
        when(mockWorkspaceFactory.createWorkspace(workspaceId))
                .thenReturn(mockWorkspace);
        ArrayList<EditorClientId> editorClientIds =
                Lists.newArrayList(new EditorClientId("sessionId", 1L),
                        new EditorClientId("sessionId", 2L));
        when(mockWorkspace.removeEditorClients("sessionId")).thenReturn(
                editorClientIds);
        spy.getOrRegisterWorkspace(workspaceId);

        spy.exitWorkspace("admin", "sessionId", "patrick",
                "admin@example.com");

        verify(mockWorkspace).removeEditorClients("sessionId");
        verify(mockWorkspace, times(2)).publish(eventCaptor.capture());

        ExitWorkspace exitWorkspace1 = eventCaptor.getAllValues().get(0);
        assertThat(exitWorkspace1.getEditorClientId())
                .isEqualTo(editorClientIds.get(0));
        assertThat(exitWorkspace1.getPerson().getName()).isEqualTo("patrick");
        verify(mockWorkspace).publish(exitWorkspace1);

        ExitWorkspace exitWorkspace2 = eventCaptor.getAllValues().get(1);
        assertThat(exitWorkspace2.getEditorClientId())
                .isEqualTo(editorClientIds.get(1));
        verify(mockWorkspace).publish(exitWorkspace2);
        verify(gravatarServiceImpl, times(2)).getUserImageUrl(16,
                "admin@example.com");
    }

    @Test
    @UseRealWorkspaceManagerFactory(false)
    public void testProjectIterationUpdate() throws Exception {
        HProjectIteration projectIteration =
                makeHProjectIteration("project", "master");
        WorkspaceId workspaceId = GWTTestData.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                                .getProjectIterationId().getProjectSlug(),
                        workspaceId
                                .getProjectIterationId()
                                .getIterationSlug()))
                .thenReturn(projectIteration);
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);

        when(
                validationServiceImpl.getValidationActions(projectIteration
                                .getProject().getSlug(),
                        projectIteration.getSlug()))
                .thenReturn(new ArrayList<ValidationAction>());

        TranslationWorkspaceManagerImpl spy = spy(manager);
        when(mockWorkspaceFactory.createWorkspace(workspaceId))
                .thenReturn(mockWorkspace);
        spy.getOrRegisterWorkspace(workspaceId);

        // update project iteration
        spy.projectIterationUpdate(projectIteration, oldProjectSlug,
                oldIterationSlug);
        verify(mockWorkspace).publish(isA(WorkspaceContextUpdate.class));
    }

    @Test
    @UseRealWorkspaceManagerFactory(false)
    public void testProjectUpdate() throws Exception {
        // Given: we have 3 iteration in the project
        HProjectIteration master = makeHProjectIteration("project", "master");
        HProjectIteration iteration1 = makeHProjectIteration("project", "1");
        HProjectIteration iteration2 = makeHProjectIteration("project", "2");
        HProject project = master.getProject();
        project.getProjectIterations().clear();
        project.getProjectIterations().add(master);
        project.getProjectIterations().add(iteration1);
        project.getProjectIterations().add(iteration2);

        when(entityManager.find(HProject.class, project.getId()))
                .thenReturn(project);
        doReturn(mockWorkspaceMaster).when(mockWorkspaceFactory).createWorkspace(matchIteration("master"));
        doReturn(mockWorkspace1).when(mockWorkspaceFactory).createWorkspace(matchIteration("1"));
        doReturn(mockWorkspace2).when(mockWorkspaceFactory).createWorkspace(matchIteration("2"));

        manager.getOrRegisterWorkspace(new WorkspaceId(new ProjectIterationId("oldProject", "master",
                ProjectType.File), LocaleId.EN_US));
        manager.getOrRegisterWorkspace(new WorkspaceId(new ProjectIterationId("oldProject", "1",
                ProjectType.File), LocaleId.EN_US));
        manager.getOrRegisterWorkspace(new WorkspaceId(new ProjectIterationId("oldProject", "2",
                ProjectType.File), LocaleId.EN_US));

        manager.projectUpdate(master.getProject(), "oldProject");

        verify(mockWorkspaceMaster).publish(matchEvent("oldProject", "project"));
        verify(mockWorkspace1).publish(matchEvent("oldProject", "project"));
        verify(mockWorkspace2).publish(matchEvent("oldProject", "project"));
    }

    private static WorkspaceContextUpdate matchEvent(String oldProjectSlug,
            String newProjectSlug) {
        return argThat(new AssertionMatcher<WorkspaceContextUpdate>() {
            @Override
            public void assertion(WorkspaceContextUpdate actual)
                    throws AssertionError {
                assertThat(actual.getOldProjectSlug()).isEqualTo(oldProjectSlug);
                assertThat(actual.getNewProjectSlug()).isEqualTo(newProjectSlug);
            }
        });
    }

    private static WorkspaceId matchIteration(String iterationSlug) {
        return argThat(new AssertionMatcher<WorkspaceId>() {
            @Override
            public void assertion(WorkspaceId actual)
                    throws AssertionError {
                assertThat(actual.getProjectIterationId().getIterationSlug())
                        .isEqualTo(iterationSlug);
            }
        });
    }
}

