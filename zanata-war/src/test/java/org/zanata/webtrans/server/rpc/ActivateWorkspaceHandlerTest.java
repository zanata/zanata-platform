package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.jboss.seam.web.ServletContexts;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.EntityStatus;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HIterationProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.EnterWorkspace;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ActivateWorkspaceHandlerTest
{
   public static final String HTTP_SESSION_ID = "httpSessionId";
   private ActivateWorkspaceHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private TranslationWorkspaceManager translationWorkspaceManager;
   @Mock
   private TranslationWorkspace translationWorkspace;
   @Mock
   private GravatarService gravatarServiceImpl;
   @Mock
   private ProjectDAO projectDAO;
   @Mock
   private ProjectIterationDAO projectIterationDAO;
   @Mock
   private LocaleService localeServiceImpl;
   private Person person;
   @Captor
   private ArgumentCaptor<EnterWorkspace> enterWorkspaceEventCaptor;
   @Captor
   private ArgumentCaptor<EditorClientId> editorClientIdCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      ActivateWorkspaceHandler activateWorkspaceHandler = SeamAutowire.instance()
            .use("identity", identity)
            .use("translationWorkspaceManager", translationWorkspaceManager)
            .use("projectDAO", projectDAO)
            .use("projectIterationDAO", projectIterationDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .ignoreNonResolvable()
            .autowire(ActivateWorkspaceHandler.class);
      // @formatter:on
      handler = spy(activateWorkspaceHandler);
      person = TestFixture.person();
      doReturn(person).when(handler).retrievePerson();
      doReturn(HTTP_SESSION_ID).when(handler).getHttpSessionId();
   }

   @Test
   public void testExecute() throws Exception
   {
      WorkspaceId workspaceId = TestFixture.workspaceId();
      ActivateWorkspaceAction action = new ActivateWorkspaceAction(workspaceId);
      when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId)).thenReturn(translationWorkspace);
      HLocale hLocale = new HLocale(workspaceId.getLocaleId());
      when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId())).thenReturn(hLocale);
      ProjectIterationId projectIterationId = workspaceId.getProjectIterationId();
      HIterationProject hProject = new HIterationProject();
      when(projectDAO.getBySlug(projectIterationId.getProjectSlug())).thenReturn(hProject);
      HProjectIteration hProjectIteration = new HProjectIteration();
      when(projectIterationDAO.getBySlug(projectIterationId.getProjectSlug(), projectIterationId.getIterationSlug())).thenReturn(hProjectIteration);
      when(identity.hasPermission("modify-translation", hProject, hLocale)).thenReturn(true);
      when(identity.hasPermission("glossary-update", "")).thenReturn(true);

      ActivateWorkspaceResult result = handler.execute(action, null);

      verify(identity).checkLoggedIn();
      verify(translationWorkspace).addEditorClient(eq(HTTP_SESSION_ID), editorClientIdCaptor.capture(), eq(person.getId()));
      EditorClientId editorClientId = editorClientIdCaptor.getValue();
      assertThat(editorClientId.getHttpSessionId(), Matchers.equalTo(HTTP_SESSION_ID));

      verify(translationWorkspace).publish(enterWorkspaceEventCaptor.capture());
      EnterWorkspace enterWorkspace = enterWorkspaceEventCaptor.getValue();
      assertThat(enterWorkspace.getPerson(), Matchers.equalTo(person));
      assertThat(enterWorkspace.getEditorClientId(), Matchers.equalTo(editorClientId));

      Identity userIdentity = result.getIdentity();
      assertThat(userIdentity.getPerson(), Matchers.equalTo(person));
      assertThat(userIdentity.getEditorClientId(), Matchers.equalTo(editorClientId));

      UserWorkspaceContext userWorkspaceContext = result.getUserWorkspaceContext();
      assertThat(userWorkspaceContext.hasGlossaryUpdateAccess(), Matchers.equalTo(true));
      assertThat(userWorkspaceContext.isProjectActive(), Matchers.equalTo(true));
      assertThat(userWorkspaceContext.hasWriteAccess(), Matchers.equalTo(true));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
