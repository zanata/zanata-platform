package org.zanata.webtrans.client.service;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.HasTransUnitEditData;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class UserSessionServiceTest
{
   private UserSessionService service;
   @Mock
   private EventBus eventBus;
   @Mock
   private DistinctColor distinctColor;
   @Mock
   private HasManageUserPanel panel;
   @Mock
   private HasTransUnitEditData hasTransUnitData;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      service = new UserSessionService(eventBus, distinctColor);

      verify(eventBus).addHandler(TransUnitEditEvent.getType(), service);
   }

   private static Person person()
   {
      return new Person(new PersonId("pid"), "name", null);
   }

   private static EditorClientId editorClientId()
   {
      return new EditorClientId("session", 1);
   }

   @Test
   public void canAddUser()
   {
      EditorClientId editorClientId = editorClientId();
      UserPanelSessionItem sessionItem = new UserPanelSessionItem(panel, person());

      service.addUser(editorClientId, sessionItem);

      assertThat(service.getUserSessionMap(), Matchers.hasEntry(editorClientId, sessionItem));
   }

   @Test
   public void onTransUnitEditEvent()
   {
      // Given:
      EditorClientId editorClientId = editorClientId();
      UserPanelSessionItem sessionItem = new UserPanelSessionItem(panel, person());
      TransUnit transUnit = TestFixture.makeTransUnit(2);
      service.addUser(editorClientId, sessionItem);
      when(hasTransUnitData.getEditorClientId()).thenReturn(editorClientId);
      when(hasTransUnitData.getSelectedTransUnit()).thenReturn(transUnit);

      // When:
      service.onTransUnitEdit(new TransUnitEditEvent(hasTransUnitData));

      // Then:
      assertThat(service.getUserPanel(editorClientId).getSelectedTransUnit(), Matchers.sameInstance(transUnit));
      assertThat(service.getUserSessionMap().get(editorClientId).getSelectedTransUnit(), Matchers.sameInstance(transUnit));
   }

   @Test
   public void canRemoveUser()
   {
      EditorClientId editorClientId = editorClientId();
      UserPanelSessionItem sessionItem = new UserPanelSessionItem(panel, person());
      service.addUser(editorClientId, sessionItem);
      service.removeUser(editorClientId);

      assertThat(service.getUserSessionMap().size(), Matchers.is(0));
      verify(distinctColor).releaseColor(editorClientId);
   }

   @Test
   public void canGetColor()
   {
      EditorClientId editorClientId = editorClientId();
      service.getColor(editorClientId);
      verify(distinctColor).getOrCreateColor(editorClientId);
   }

}
