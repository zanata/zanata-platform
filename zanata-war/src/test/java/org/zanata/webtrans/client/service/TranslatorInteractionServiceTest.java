package org.zanata.webtrans.client.service;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.TransUnitEditAction;

import static org.hamcrest.MatcherAssert.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TranslatorInteractionServiceTest
{
   private TranslatorInteractionService service;
   @Mock
   private CachingDispatchAsync dispatcher;
   private Identity identity;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      identity = new Identity(new EditorClientId("sessionId", 1), new Person(new PersonId("pid"), "name", "url"));
      service = new TranslatorInteractionService(identity, dispatcher);
   }

   @Test
   public void canGetEditorClientId()
   {
      EditorClientId currentEditorClientId = service.getCurrentEditorClientId();

      assertThat(currentEditorClientId, Matchers.equalTo(identity.getEditorClientId()));
   }

   @Test
   public void canCallServerOnSelection()
   {
      TransUnit selectedTransUnit = TestFixture.makeTransUnit(1);
      service.transUnitSelected(selectedTransUnit);

      ArgumentCaptor<TransUnitEditAction> actionCaptor = ArgumentCaptor.forClass(TransUnitEditAction.class);
      Mockito.verify(dispatcher).execute(actionCaptor.capture(), Mockito.isA(NoOpAsyncCallback.class));
      TransUnitEditAction action = actionCaptor.getValue();
      assertThat(action.getPerson(), Matchers.sameInstance(identity.getPerson()));
      assertThat(action.getSelectedTransUnit(), Matchers.sameInstance(selectedTransUnit));
   }
}
