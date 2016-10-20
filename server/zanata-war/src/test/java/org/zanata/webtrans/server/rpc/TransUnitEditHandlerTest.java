package org.zanata.webtrans.server.rpc;

import org.hamcrest.Matchers;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.model.TestFixture;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.server.TranslationWorkspace;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.TransUnitEdit;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class TransUnitEditHandlerTest extends ZanataTest {
    @Inject @Any
    private TransUnitEditHandler handler;
    @Produces @Mock
    private ZanataIdentity identity;
    @Produces @Mock
    private TranslationWorkspaceManager translationWorkspaceManager;
    @Produces @Mock
    private TranslationWorkspace translationWorkspace;
    @Captor
    private ArgumentCaptor<TransUnitEdit> eventCaptor;

    @Test
    @InRequestScope
    public void testExecute() throws Exception {
        Person person = TestFixture.person();
        TransUnit selectedTransUnit = TestFixture.makeTransUnit(1);
        WorkspaceId workspaceId = TestFixture.workspaceId();
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        TransUnitEditAction action =
                new TransUnitEditAction(person, selectedTransUnit.getId());
        action.setWorkspaceId(workspaceId);
        action.setEditorClientId(editorClientId);
        when(translationWorkspaceManager.getOrRegisterWorkspace(workspaceId))
                .thenReturn(translationWorkspace);

        handler.execute(action, null);

        verify(identity).checkLoggedIn();
        verify(translationWorkspace).updateUserSelection(editorClientId,
                selectedTransUnit.getId());
        verify(translationWorkspace).publish(eventCaptor.capture());
        TransUnitEdit transUnitEdit = eventCaptor.getValue();
        assertThat(transUnitEdit.getEditorClientId(),
                Matchers.sameInstance(editorClientId));
        assertThat(transUnitEdit.getPerson(), Matchers.sameInstance(person));
        assertThat(transUnitEdit.getSelectedTransUnitId(),
                Matchers.sameInstance(selectedTransUnit.getId()));
    }

    @Test
    @InRequestScope
    public void testRollback() throws Exception {
        handler.rollback(null, null, null);
    }
}
