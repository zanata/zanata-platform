package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChat;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class PublishWorkspaceChatHandlerTest
{
   private PublishWorkspaceChatHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private TranslationWorkspaceManager translationWorkspaceManager;
   @Mock
   private TranslationWorkspace translationWorkspace;
   @Captor
   private ArgumentCaptor<PublishWorkspaceChat> eventCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("translationWorkspaceManager", translationWorkspaceManager)
            .ignoreNonResolvable()
            .autowire(PublishWorkspaceChatHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws Exception
   {
      WorkspaceId workspaceId = TestFixture.workspaceId();
      PublishWorkspaceChatAction action = new PublishWorkspaceChatAction("admin", "hi", HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
      action.setWorkspaceId(workspaceId);
      when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId)).thenReturn(translationWorkspace);

      handler.execute(action, null);

      verify(identity).checkLoggedIn();
      verify(translationWorkspace).publish(eventCaptor.capture());
      PublishWorkspaceChat chat = eventCaptor.getValue();
      assertThat(chat.getPersonId(), Matchers.equalTo("admin"));
      assertThat(chat.getMsg(), Matchers.equalTo("hi"));
      assertThat(chat.getMessageType(), Matchers.equalTo(HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
