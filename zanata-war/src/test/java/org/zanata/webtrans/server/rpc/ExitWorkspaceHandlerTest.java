package org.zanata.webtrans.server.rpc;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.ExitWorkspaceAction;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ExitWorkspaceHandlerTest {
    private ExitWorkspaceHandler handler;
    @Mock
    private ZanataIdentity identity;
    @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Mock
    private TranslationWorkspace translationWorkspace;

    @BeforeMethod
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        // @formatter:off
      handler = SeamAutowire.instance()
            .reset()
            .use("identity", identity)
            .use("translationWorkspaceManager", translationWorkspaceManager)
            .ignoreNonResolvable()
            .autowire(ExitWorkspaceHandler.class);
      // @formatter:on
    }

    @Test
    public void testExecute() throws Exception {
        Person person = TestFixture.person();
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        WorkspaceId workspaceId = TestFixture.workspaceId();
        when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId))
                .thenReturn(translationWorkspace);
        ExitWorkspaceAction action = new ExitWorkspaceAction(person);
        action.setEditorClientId(editorClientId);
        action.setWorkspaceId(workspaceId);

        handler.execute(action, null);

        verify(identity).checkLoggedIn();
        verify(translationWorkspace).removeEditorClient(
                action.getEditorClientId());
    }

    @Test
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
