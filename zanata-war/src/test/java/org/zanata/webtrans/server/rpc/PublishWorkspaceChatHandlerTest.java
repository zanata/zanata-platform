package org.zanata.webtrans.server.rpc;

import org.mockito.Mock;
import org.mockito.Mockito;
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
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;

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
   }
}
