package org.zanata.webtrans.server.rpc;

import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChat;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import org.zanata.webtrans.test.GWTTestData;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class PublishWorkspaceChatHandlerTest extends ZanataTest {
    @Inject @Any
    private PublishWorkspaceChatHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Produces @Mock
    private TranslationWorkspace translationWorkspace;
    @Captor
    private ArgumentCaptor<PublishWorkspaceChat> eventCaptor;

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        WorkspaceId workspaceId = GWTTestData.workspaceId();
        PublishWorkspaceChatAction action =
                new PublishWorkspaceChatAction("admin", "hi",
                        HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
        action.setWorkspaceId(workspaceId);
        when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId))
                .thenReturn(translationWorkspace);

        handler.execute(action, null);

        verify(identity).checkLoggedIn();
        verify(translationWorkspace).publish(eventCaptor.capture());
        PublishWorkspaceChat chat = eventCaptor.getValue();
        assertThat(chat.getPersonId()).isEqualTo("admin");
        assertThat(chat.getMsg()).isEqualTo("hi");
        assertThat(chat.getMessageType())
                .isEqualTo(HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
