package org.zanata.webtrans.server;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.service.ValidationService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.test.LambdaMatcher;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(Enclosed.class)
public class TranslationWorkspaceManagerImplTest {

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

    @RunWith(CdiUnitRunner.class)
    public static class RealWorkspaceFactoryTest extends ZanataTest {
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
//    @Produces @Mock TranslationWorkspaceFactory translationWorkspaceFactory;

        @Before
        public void beforeMethod() throws Exception {
//        MockitoAnnotations.initMocks(this);
            // @formatter:off
//      manager = SeamAutowire.instance()
//            .reset()
//            .use("gravatarServiceImpl", gravatarServiceImpl)
//            .use("projectIterationDAO", projectIterationDAO)
//            .use("localeServiceImpl", localeServiceImpl)
//            .use("validationServiceImpl", validationServiceImpl)
//            .use("entityManager", entityManager)
//            .ignoreNonResolvable()
//            .autowire(TranslationWorkspaceManagerImpl.class);
            // @formatter:on
//        when(translationWorkspaceFactory.createWorkspace(any())).thenReturn(mockWorkspace);
        }

        @Test(expected = NoSuchWorkspaceException.class)
        public void testRegisterInvalidWorkspace() throws Exception {
            WorkspaceId workspaceId = TestFixture.workspaceId();
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
            WorkspaceId workspaceId = TestFixture.workspaceId();
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
            WorkspaceId workspaceId = TestFixture.workspaceId();
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
            WorkspaceId workspaceId = TestFixture.workspaceId();
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
            WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
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
            assertThat(context.getWorkspaceId(), Matchers.equalTo(workspaceId));
            assertThat(context.getLocaleName(), Matchers.equalTo("German"));
            assertThat(context.getWorkspaceName(),
                    Matchers.equalTo("project (master)"));
        }

        @Test
        public void testGetRegisteredNewWorkspace() throws Exception {
            WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
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
            assertThat(anotherWorkspace, Matchers.sameInstance(workspace));
        }

        @Test
        public void testExitWorkspaceWithNullSessionId() throws Exception {
            TranslationWorkspaceManagerImpl spy = spy(manager);

            spy.exitWorkspace("admin", null, "Administrator", "admin@example.com");

            verifyZeroInteractions(accountDAO);
        }
    }

    @RunWith(CdiUnitRunner.class)
    public static class MockWorkspaceFactoryTest {

        @Inject
        private TranslationWorkspaceManagerImpl manager;

        @Produces
        @Mock
        private AccountDAO accountDAO;
        @Produces
        @Mock
        private GravatarService gravatarServiceImpl;
        @Produces
        @Mock
        private ProjectIterationDAO projectIterationDAO;
        @Produces
        @Mock
        private LocaleService localeServiceImpl;
        @Produces
        @Mock
        private ValidationService validationServiceImpl;
        @Produces
        @Mock
        private TranslationWorkspace mockWorkspace;
        @Mock
        private TranslationWorkspace mockWorkspaceMaster;
        @Mock
        private TranslationWorkspace mockWorkspace1;
        @Mock
        private TranslationWorkspace mockWorkspace2;
        @Captor
        private ArgumentCaptor<ExitWorkspace> eventCaptor;
        private Optional<String> oldProjectSlug = Optional.absent();
        private Optional<String> oldIterationSlug = Optional.absent();
        @Produces
        @Mock
        private EntityManager entityManager;
        @Produces
        @Mock
        TranslationWorkspaceFactory translationWorkspaceFactory;

        @Test
        public void testExitWorkspace() throws Exception {
            HProjectIteration projectIteration =
                    makeHProjectIteration("project", "master");
            WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
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
            when(translationWorkspaceFactory.createWorkspace(workspaceId))
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
            assertThat(exitWorkspace1.getEditorClientId(),
                    Matchers.equalTo(editorClientIds.get(0)));
            assertThat(exitWorkspace1.getPerson().getName(),
                    Matchers.equalTo("patrick"));
            verify(mockWorkspace).publish(exitWorkspace1);

            ExitWorkspace exitWorkspace2 = eventCaptor.getAllValues().get(1);
            assertThat(exitWorkspace2.getEditorClientId(),
                    Matchers.equalTo(editorClientIds.get(1)));
            verify(mockWorkspace).publish(exitWorkspace2);
            verify(gravatarServiceImpl, times(2)).getUserImageUrl(16,
                    "admin@example.com");
        }

        @Test
        public void testProjectIterationUpdate() throws Exception {
            HProjectIteration projectIteration =
                    makeHProjectIteration("project", "master");
            WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
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
            when(translationWorkspaceFactory.createWorkspace(workspaceId))
                    .thenReturn(mockWorkspace);
            spy.getOrRegisterWorkspace(workspaceId);

            // update project iteration
            spy.projectIterationUpdate(projectIteration, oldProjectSlug,
                    oldIterationSlug);
            verify(mockWorkspace).publish(isA(WorkspaceContextUpdate.class));
        }

        @Test
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
            doReturn(mockWorkspaceMaster).when(translationWorkspaceFactory).createWorkspace(matchIteration("master"));
            doReturn(mockWorkspace1).when(translationWorkspaceFactory).createWorkspace(matchIteration("1"));
            doReturn(mockWorkspace2).when(translationWorkspaceFactory).createWorkspace(matchIteration("2"));

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
            return argThat(new LambdaMatcher<>(wcu ->
                    wcu.getOldProjectSlug().equals(oldProjectSlug) &&
                            wcu.getNewProjectSlug().equals(newProjectSlug)));
        }

        private static WorkspaceId matchIteration(String iterationSlug) {
            return argThat(new LambdaMatcher<>(
                    workspaceId -> workspaceId.getProjectIterationId()
                            .getIterationSlug().equals(iterationSlug)));
        }
    }
}
