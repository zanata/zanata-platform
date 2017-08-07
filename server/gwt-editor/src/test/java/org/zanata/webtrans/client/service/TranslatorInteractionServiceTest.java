package org.zanata.webtrans.client.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.test.GWTTestData;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.zanata.webtrans.test.GWTTestData.makeTransUnit;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TranslatorInteractionServiceTest {
    private TranslatorInteractionService service;
    @Mock
    private CachingDispatchAsync dispatcher;
    private Identity identity;
    @Captor
    private ArgumentCaptor<TransUnitEditAction> actionCaptor;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        identity =
                new Identity(new EditorClientId("sessionId", 1), new Person(
                        new PersonId("pid"), "name", "url"));
        service = new TranslatorInteractionService(identity, dispatcher);
    }

    @Test
    public void canGetEditorClientId() {
        EditorClientId currentEditorClientId =
                service.getCurrentEditorClientId();

        assertThat(currentEditorClientId).isEqualTo(identity.getEditorClientId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void canCallServerOnSelection() {
        TransUnit selectedTransUnit = makeTransUnit(1);
        service.transUnitSelected(selectedTransUnit);

        verify(dispatcher).execute(actionCaptor.capture(),
                Mockito.isA(NoOpAsyncCallback.class));
        TransUnitEditAction action = actionCaptor.getValue();
        assertThat(action.getPerson()).isSameAs(identity.getPerson());
        assertThat(action.getSelectedTransUnitId())
                .isSameAs(selectedTransUnit.getId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onPersonExit() {
        TransUnit selectedTransUnit = makeTransUnit(1);
        Person person = GWTTestData.person();

        service.personExit(person, selectedTransUnit.getId());

        verify(dispatcher).execute(actionCaptor.capture(),
                Mockito.isA(NoOpAsyncCallback.class));
        TransUnitEditAction action = actionCaptor.getValue();
        assertThat(action.getPerson()).isSameAs(person);
        assertThat(action.getSelectedTransUnitId())
                .isSameAs(selectedTransUnit.getId());
    }
}
