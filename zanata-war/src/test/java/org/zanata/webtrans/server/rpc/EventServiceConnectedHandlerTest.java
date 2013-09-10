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
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.EventServiceConnectedAction;

import net.customware.gwt.dispatch.shared.ActionException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class EventServiceConnectedHandlerTest
{
   private EventServiceConnectedHandler handler;
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
            .autowire(EventServiceConnectedHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws ActionException
   {
      WorkspaceId workspaceId = TestFixture.workspaceId();
      EditorClientId editorClientId = new EditorClientId("sessionId", 1);
      EventServiceConnectedAction action = new EventServiceConnectedAction("connectionId");
      action.setWorkspaceId(workspaceId);
      action.setEditorClientId(editorClientId);
      when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId)).thenReturn(translationWorkspace);

      handler.execute(action, null);

      verify(identity).checkLoggedIn();
      verify(translationWorkspace).onEventServiceConnected(editorClientId, "connectionId");
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
