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

import org.apache.deltaspike.core.api.common.DeltaSpike;
import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.ProjectDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.GravatarService;
import org.zanata.service.LocaleService;
import org.zanata.test.CdiUnitRunner;
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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class ActivateWorkspaceHandlerTest extends ZanataTest {
    public static final String HTTP_SESSION_ID = "httpSessionId";
    @Inject @Any
    private ActivateWorkspaceHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Produces @Mock
    private WorkspaceContext workspaceContext;
    @Produces @Mock
    private TranslationWorkspace translationWorkspace;
    @Produces @Mock
    private GravatarService gravatarServiceImpl;
    @Produces @Mock
    private AccountDAO accountDAO;
    @Produces @Mock
    private ProjectDAO projectDAO;
    @Produces @Mock
    private ProjectIterationDAO projectIterationDAO;
    @Produces @Mock
    private LocaleService localeServiceImpl;
    @Produces @Mock @DeltaSpike
    private HttpSession httpSession;
    private Person person;
    @Produces @Mock @Authenticated
    private HAccount hAccount;
    @Mock
    private HPerson authenticatedPerson;
    @Captor
    private ArgumentCaptor<EnterWorkspace> enterWorkspaceEventCaptor;
    @Captor
    private ArgumentCaptor<EditorClientId> editorClientIdCaptor;
    @Produces @Mock
    private LoadOptionsHandler loadOptionsHandler;
    @Produces @Mock
    private GetValidationRulesHandler getValidationRulesHandler;

    private ValidationFactory validationFactory;

    @Before
    public void setUp() throws Exception {
        handler = spy(handler);
        person = TestFixture.person();
        doReturn(person).when(handler).retrievePerson();
        doReturn(HTTP_SESSION_ID).when(handler).getHttpSessionId();
        long accountId = 7;
        when(hAccount.getId()).thenReturn(accountId);
        when(hAccount.getUsername()).thenReturn("pid");
        when(hAccount.getPerson()).thenReturn(authenticatedPerson);
        when(authenticatedPerson.getName()).thenReturn("Demo");
        when(httpSession.getId()).thenReturn(HTTP_SESSION_ID);
        when(accountDAO.findById(accountId, true)).thenReturn(hAccount);

        ValidationMessages message =
                Gwti18nReader.create(ValidationMessages.class);
        validationFactory = new ValidationFactory(message);
    }

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        WorkspaceId workspaceId = TestFixture.workspaceId();
        ActivateWorkspaceAction action =
                new ActivateWorkspaceAction(workspaceId);
        when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId))
                .thenReturn(translationWorkspace);
        HLocale hLocale = new HLocale(workspaceId.getLocaleId());
        when(localeServiceImpl.getByLocaleId(workspaceId.getLocaleId()))
                .thenReturn(hLocale);
        ProjectIterationId projectIterationId =
                workspaceId.getProjectIterationId();
        HProject hProject = new HProject();
        when(projectDAO.getBySlug(projectIterationId.getProjectSlug()))
                .thenReturn(hProject);
        HProjectIteration hProjectIteration = new HProjectIteration();
        when(
                projectIterationDAO.getBySlug(
                        projectIterationId.getProjectSlug(),
                        projectIterationId.getIterationSlug())).thenReturn(
                hProjectIteration);
        when(identity.hasPermissionWithAnyTargets("modify-translation",
                hProject, hLocale))
                .thenReturn(true);
        when(identity.hasPermission("", "glossary-update")).thenReturn(true);
        LoadOptionsResult optionsResult =
                new LoadOptionsResult(new UserConfigHolder().getState());
        when(
                loadOptionsHandler.execute(isA(LoadOptionsAction.class),
                        any(ExecutionContext.class))).thenReturn(optionsResult);

        when(translationWorkspace.getWorkspaceContext()).thenReturn(
                workspaceContext);
        when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);

        Collection<ValidationAction> validationList =
                validationFactory.getAllValidationActions().values();
        Map<ValidationId, State> validationStates =
                new HashMap<ValidationId, State>();
        for (ValidationAction valAction : validationList) {
            validationStates.put(valAction.getId(), valAction.getState());
        }

        GetValidationRulesResult validationResult =
                new GetValidationRulesResult(validationStates);
        when(
                getValidationRulesHandler.execute(
                        isA(GetValidationRulesAction.class),
                        any(ExecutionContext.class))).thenReturn(
                validationResult);

        ActivateWorkspaceResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        verify(translationWorkspace).addEditorClient(eq(HTTP_SESSION_ID),
                editorClientIdCaptor.capture(), eq(person.getId()));
        EditorClientId editorClientId = editorClientIdCaptor.getValue();
        assertThat(editorClientId.getHttpSessionId(),
                Matchers.equalTo(HTTP_SESSION_ID));

        verify(translationWorkspace).publish(
                enterWorkspaceEventCaptor.capture());
        EnterWorkspace enterWorkspace = enterWorkspaceEventCaptor.getValue();
        assertThat(enterWorkspace.getPerson(), Matchers.equalTo(person));
        assertThat(enterWorkspace.getEditorClientId(),
                Matchers.equalTo(editorClientId));

        Identity userIdentity = result.getIdentity();
        assertThat(userIdentity.getPerson(), Matchers.equalTo(person));
        assertThat(userIdentity.getEditorClientId(),
                Matchers.equalTo(editorClientId));

        UserWorkspaceContext userWorkspaceContext =
                result.getUserWorkspaceContext();
        assertThat(userWorkspaceContext.getWorkspaceRestrictions()
                .isHasGlossaryUpdateAccess(), Matchers.equalTo(true));
        assertThat(userWorkspaceContext.getWorkspaceRestrictions()
                .isProjectActive(), Matchers.equalTo(true));
        assertThat(userWorkspaceContext.getWorkspaceRestrictions()
                .isHasEditTranslationAccess(), Matchers.equalTo(true));

        assertThat(result.getStoredUserConfiguration(),
                Matchers.sameInstance(optionsResult.getConfiguration()));
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
