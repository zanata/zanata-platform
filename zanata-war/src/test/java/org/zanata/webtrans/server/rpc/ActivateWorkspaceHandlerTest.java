package org.zanata.webtrans.server.rpc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.server.ExecutionContext;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.ValidationMessages;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.server.locale.Gwti18nReader;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceAction;
import org.zanata.webtrans.shared.rpc.ActivateWorkspaceResult;
import org.zanata.webtrans.shared.rpc.EnterWorkspace;
import org.zanata.webtrans.shared.rpc.GetValidationRulesAction;
import org.zanata.webtrans.shared.rpc.GetValidationRulesResult;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.validation.ValidationFactory;

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
   private WorkspaceContext workspaceContext;
   @Mock
   private TranslationWorkspace translationWorkspace;
   @Mock
   private GravatarService gravatarServiceImpl;
   @Mock
   private AccountDAO accountDAO;
   @Mock
   private ProjectDAO projectDAO;
   @Mock
   private ProjectIterationDAO projectIterationDAO;
   @Mock
   private LocaleService localeServiceImpl;
   private Person person;
   @Mock
   private HAccount hAccount;
   @Captor
   private ArgumentCaptor<EnterWorkspace> enterWorkspaceEventCaptor;
   @Captor
   private ArgumentCaptor<EditorClientId> editorClientIdCaptor;
   @Mock
   private LoadOptionsHandler loadOptionsHandler;
   @Mock
   private GetValidationRulesHandler getValidationRulesHandler;

   private ValidationFactory validationFactory;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      ActivateWorkspaceHandler activateWorkspaceHandler = SeamAutowire.instance()
            .use("identity", identity)
            .use("translationWorkspaceManager", translationWorkspaceManager)
            .use("accountDAO", accountDAO)
            .use("projectDAO", projectDAO)
            .use("projectIterationDAO", projectIterationDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .use("webtrans.gwt.LoadOptionsHandler", loadOptionsHandler)
            .use("webtrans.gwt.GetValidationRulesHandler", getValidationRulesHandler)
            .ignoreNonResolvable()
            .autowire(ActivateWorkspaceHandler.class);
      // @formatter:on
      handler = spy(activateWorkspaceHandler);
      person = TestFixture.person();
      doReturn(person).when(handler).retrievePerson();
      doReturn(HTTP_SESSION_ID).when(handler).getHttpSessionId();
       
      ValidationMessages message = Gwti18nReader.create(ValidationMessages.class);
      validationFactory = new ValidationFactory(message);
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
      HProject hProject = new HProject();
      when(projectDAO.getBySlug(projectIterationId.getProjectSlug())).thenReturn(hProject);
      HProjectIteration hProjectIteration = new HProjectIteration();
      when(projectIterationDAO.getBySlug(projectIterationId.getProjectSlug(), projectIterationId.getIterationSlug())).thenReturn(hProjectIteration);
      when(identity.hasPermission("modify-translation", hProject, hLocale)).thenReturn(true);
      when(identity.hasPermission("glossary-update", "")).thenReturn(true);
      LoadOptionsResult optionsResult = new LoadOptionsResult(new UserConfigHolder().getState());
      when(loadOptionsHandler.execute(isA(LoadOptionsAction.class), any(ExecutionContext.class))).thenReturn(optionsResult);
      
      when(translationWorkspace.getWorkspaceContext()).thenReturn(workspaceContext);
      when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);

      Collection<ValidationAction> validationList = validationFactory.getAllValidationActions().values();
      Map<ValidationId, State> validationStates = new HashMap<ValidationId, State>();
      for (ValidationAction valAction : validationList)
      {
         validationStates.put(valAction.getId(), valAction.getState());
      }
      
      GetValidationRulesResult validationResult = new GetValidationRulesResult(validationStates);
      when(getValidationRulesHandler.execute(isA(GetValidationRulesAction.class), any(ExecutionContext.class))).thenReturn(validationResult);

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
      assertThat(userWorkspaceContext.getWorkspaceRestrictions().isHasGlossaryUpdateAccess(), Matchers.equalTo(true));
      assertThat(userWorkspaceContext.getWorkspaceRestrictions().isProjectActive(), Matchers.equalTo(true));
      assertThat(userWorkspaceContext.getWorkspaceRestrictions().isHasEditTranslationAccess(), Matchers.equalTo(true));

      assertThat(result.getStoredUserConfiguration(), Matchers.sameInstance(optionsResult.getConfiguration()));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
