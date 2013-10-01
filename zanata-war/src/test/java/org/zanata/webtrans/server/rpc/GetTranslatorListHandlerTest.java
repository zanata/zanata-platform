package org.zanata.webtrans.server.rpc;

import java.util.Map;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.dao.AccountDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.GravatarService;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import com.google.common.collect.Maps;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class GetTranslatorListHandlerTest {
    private GetTranslatorListHandler handler;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Mock
    private TranslationWorkspace translationWorkspace;
    @Mock
    private AccountDAO accountDAO;
    @Mock
    private GravatarService gravatarServiceImpl;
    private Map<EditorClientId, PersonSessionDetails> users;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("translationWorkspaceManager", translationWorkspaceManager)
            .use("accountDAO", accountDAO)
            .use("gravatarServiceImpl", gravatarServiceImpl)
            .ignoreNonResolvable()
            .autowire(GetTranslatorListHandler.class);
      // @formatter:on
        users = Maps.newHashMap();
    }

    @Test
    public void testExecute() throws Exception {
        GetTranslatorList action = GetTranslatorList.ACTION;
        WorkspaceId workspaceId = TestFixture.workspaceId();
        Person person = TestFixture.person();
        EditorClientId editorClientId = new EditorClientId("sid", 1);
        action.setWorkspaceId(workspaceId);
        when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId))
                .thenReturn(translationWorkspace);
        users.put(editorClientId, new PersonSessionDetails(person, null));
        when(translationWorkspace.getUsers()).thenReturn(users);
        when(accountDAO.getByUsername(person.getId().toString())).thenReturn(
                createHAccount("admin@redhat.com", "pahuang"));
        when(gravatarServiceImpl.getUserImageUrl(16, "admin@redhat.com"))
                .thenReturn("gravatarUrl");

        GetTranslatorListResult result = handler.execute(action, null);

        verify(identity).checkLoggedIn();
        assertThat(result.getSize(), Matchers.equalTo(1));
        Map<EditorClientId, PersonSessionDetails> translatorList =
                result.getTranslatorList();
        assertThat(translatorList, Matchers.hasKey(editorClientId));
        assertThat(translatorList.get(editorClientId).getPerson()
                .getAvatarUrl(), Matchers.equalTo("gravatarUrl"));
        assertThat(translatorList.get(editorClientId).getPerson().getName(),
                Matchers.equalTo("pahuang"));
    }

    private static HAccount createHAccount(String email, String name) {
        HAccount hAccount = new HAccount();
        HPerson hPerson = new HPerson();
        hPerson.setEmail(email);
        hPerson.setName(name);
        hAccount.setPerson(hPerson);
        return hAccount;
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
