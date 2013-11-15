package org.zanata.webtrans.server;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.service.ValidationService;
import org.zanata.webtrans.shared.NoSuchWorkspaceException;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspace;
import org.zanata.webtrans.shared.rpc.WorkspaceContextUpdate;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TranslationWorkspaceManagerImplTest {
    private TranslationWorkspaceManagerImpl manager;

    @Mock
    private AccountDAO accountDAO;
    @Mock
    private GravatarService gravatarServiceImpl;
    @Mock
    private ProjectIterationDAO projectIterationDAO;
    @Mock
    private LocaleService localeServiceImpl;
    @Mock
    private ValidationService validationServiceImpl;
    @Mock
    private TranslationWorkspace mockWorkspace;
    @Captor
    private ArgumentCaptor<ExitWorkspace> eventCaptor;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
      manager = SeamAutowire.instance()
            .reset()
            .use("accountDAO", accountDAO)
            .use("gravatarServiceImpl", gravatarServiceImpl)
            .use("projectIterationDAO", projectIterationDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .use("validationServiceImpl", validationServiceImpl)
            .ignoreNonResolvable()
            .autowire(TranslationWorkspaceManagerImpl.class);
      // @formatter:on
    }

    private static HProjectIteration makeHProjectIteration(
            String projectSlugAndName, String iterationSlug) {
        HProjectIteration projectIteration = new HProjectIteration();
        HProject project = new HProject();
        project.setSlug(projectSlugAndName);
        project.setName(projectSlugAndName);
        projectIteration.setProject(project);
        projectIteration.setSlug(iterationSlug);
        return projectIteration;
    }

    @Test(expectedExceptions = NoSuchWorkspaceException.class)
    public void testRegisterInvalidWorkspace() throws Exception {
        WorkspaceId workspaceId = TestFixture.workspaceId();
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(null);

        manager.getOrRegisterWorkspace(workspaceId);
    }

    @Test(expectedExceptions = NoSuchWorkspaceException.class)
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

    @Test(expectedExceptions = NoSuchWorkspaceException.class)
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

    @Test(expectedExceptions = NoSuchWorkspaceException.class)
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
    public void testProjectIterationUpdate() throws Exception {
        HProjectIteration projectIteration =
                makeHProjectIteration("project", "master");
        WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(projectIteration);
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);

        when(
                validationServiceImpl.getValidationActions(projectIteration
                        .getProject().getSlug(), projectIteration.getSlug()))
                .thenReturn(new ArrayList<ValidationAction>());

        TranslationWorkspaceManagerImpl spy = spy(manager);
        doReturn(mockWorkspace).when(spy).createWorkspace(workspaceId);
        spy.getOrRegisterWorkspace(workspaceId);

        // update project iteration
        spy.projectIterationUpdate(projectIteration);
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

        TranslationWorkspaceManagerImpl spy = spy(manager);
        doNothing().when(spy).projectIterationUpdate(
                Mockito.any(HProjectIteration.class));

        spy.projectUpdate(master.getProject());

        verify(spy, atLeastOnce()).projectIterationUpdate(master);
        verify(spy, atLeastOnce()).projectIterationUpdate(iteration1);
        verify(spy, atLeastOnce()).projectIterationUpdate(iteration2);
    }

    @Test
    public void testExitWorkspaceWithNullSessionId() throws Exception {
        TranslationWorkspaceManagerImpl spy = spy(manager);
        doReturn(null).when(spy).getSessionId();

        spy.exitWorkspace("admin");

        verifyZeroInteractions(accountDAO);
    }

    @Test
    public void testExitWorkspaceWithAccount() throws Exception {
        HProjectIteration projectIteration =
                makeHProjectIteration("project", "master");
        WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(projectIteration);
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);
        TranslationWorkspaceManagerImpl spy = spy(manager);
        doReturn(mockWorkspace).when(spy).createWorkspace(workspaceId);
        doReturn("sessionId").when(spy).getSessionId();
        ArrayList<EditorClientId> editorClientIds =
                Lists.newArrayList(new EditorClientId("sessionId", 1L),
                        new EditorClientId("sessionId", 2L));
        when(mockWorkspace.removeEditorClients("sessionId")).thenReturn(
                editorClientIds);
        spy.getOrRegisterWorkspace(workspaceId);

        HAccount hAccount = new HAccount();
        HPerson person = new HPerson();
        person.setName("patrick");
        person.setEmail("pahuang@redhat.com");
        hAccount.setPerson(person);
        when(accountDAO.getByUsername("admin")).thenReturn(hAccount);

        spy.exitWorkspace("admin");

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
                "pahuang@redhat.com");
    }

    @Test
    public void testExitWorkspaceWithNullAccount() throws Exception {
        HProjectIteration projectIteration =
                makeHProjectIteration("project", "master");
        WorkspaceId workspaceId = TestFixture.workspaceId(LocaleId.DE);
        when(
                projectIterationDAO.getBySlug(workspaceId
                        .getProjectIterationId().getProjectSlug(), workspaceId
                        .getProjectIterationId().getIterationSlug()))
                .thenReturn(projectIteration);
        HLocale hLocale = new HLocale(LocaleId.DE);
        hLocale.setActive(true);
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);
        TranslationWorkspaceManagerImpl spy = spy(manager);
        doReturn(mockWorkspace).when(spy).createWorkspace(workspaceId);
        doReturn("sessionId").when(spy).getSessionId();
        ArrayList<EditorClientId> editorClientIds =
                Lists.newArrayList(new EditorClientId("sessionId", 1L),
                        new EditorClientId("sessionId", 2L));
        when(mockWorkspace.removeEditorClients("sessionId")).thenReturn(
                editorClientIds);
        spy.getOrRegisterWorkspace(workspaceId);

        spy.exitWorkspace("admin");

        verify(mockWorkspace).removeEditorClients("sessionId");
        verify(mockWorkspace, times(2)).publish(eventCaptor.capture());

        ExitWorkspace exitWorkspace1 = eventCaptor.getAllValues().get(0);
        assertThat(exitWorkspace1.getEditorClientId(),
                Matchers.equalTo(editorClientIds.get(0)));
        assertThat(exitWorkspace1.getPerson().getName(),
                Matchers.equalTo("<unknown>"));
        verify(mockWorkspace).publish(exitWorkspace1);

        ExitWorkspace exitWorkspace2 = eventCaptor.getAllValues().get(1);
        assertThat(exitWorkspace2.getEditorClientId(),
                Matchers.equalTo(editorClientIds.get(1)));
        verify(mockWorkspace).publish(exitWorkspace2);
        verify(gravatarServiceImpl, times(2)).getUserImageUrl(16, "<unknown>");
    }
}
