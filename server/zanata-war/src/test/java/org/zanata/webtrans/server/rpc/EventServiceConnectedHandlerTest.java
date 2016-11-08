package org.zanata.webtrans.server.rpc;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;

import net.customware.gwt.dispatch.shared.ActionException;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class EventServiceConnectedHandlerTest extends ZanataTest {
    @Inject @Any
    private EventServiceConnectedHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Produces @Mock
    private TranslationWorkspace translationWorkspace;

    @Test
    @InRequestScope
    public void testExecute() throws ActionException {
        WorkspaceId workspaceId = TestFixture.workspaceId();
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        EventServiceConnectedAction action =
                new EventServiceConnectedAction("connectionId");
        action.setWorkspaceId(workspaceId);
        action.setEditorClientId(editorClientId);
        when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId))
                .thenReturn(translationWorkspace);

        handler.execute(action, null);

        verify(identity).checkLoggedIn();
        verify(translationWorkspace).onEventServiceConnected(editorClientId,
                "connectionId");
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
