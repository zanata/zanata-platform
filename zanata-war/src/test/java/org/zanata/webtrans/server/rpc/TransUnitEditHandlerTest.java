package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.TransUnitEdit;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransUnitEditHandlerTest
{
   private TransUnitEditHandler handler;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private TranslationWorkspaceManager translationWorkspaceManager;
   @Mock
   private TranslationWorkspace translationWorkspace;
   @Captor
   private ArgumentCaptor<TransUnitEdit> eventCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      // @formatter:off
      handler = SeamAutowire.instance()
            .use("identity", identity)
            .use("translationWorkspaceManager", translationWorkspaceManager)
            .ignoreNonResolvable()
            .autowire(TransUnitEditHandler.class);
      // @formatter:on
   }

   @Test
   public void testExecute() throws Exception
   {
      Person person = TestFixture.person();
      TransUnit selectedTransUnit = TestFixture.makeTransUnit(1);
      WorkspaceId workspaceId = TestFixture.workspaceId();
      EditorClientId editorClientId = new EditorClientId("sessionId", 1);
      TransUnitEditAction action = new TransUnitEditAction(person, selectedTransUnit);
      action.setWorkspaceId(workspaceId);
      action.setEditorClientId(editorClientId);
      when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId)).thenReturn(translationWorkspace);

      handler.execute(action, null);

      verify(identity).checkLoggedIn();
      verify(translationWorkspace).updateUserSelection(editorClientId, selectedTransUnit);
      verify(translationWorkspace).publish(eventCaptor.capture());
      TransUnitEdit transUnitEdit = eventCaptor.getValue();
      assertThat(transUnitEdit.getEditorClientId(), Matchers.sameInstance(editorClientId));
      assertThat(transUnitEdit.getPerson(), Matchers.sameInstance(person));
      assertThat(transUnitEdit.getSelectedTransUnit(), Matchers.sameInstance(selectedTransUnit));
   }

   @Test
   public void testRollback() throws Exception
   {
      handler.rollback(null, null, null);
   }
}
