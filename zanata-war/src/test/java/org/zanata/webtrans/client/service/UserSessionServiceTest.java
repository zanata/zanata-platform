package org.zanata.webtrans.client.service;

import org.hamcrest.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.HasTransUnitEditData;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
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
   @Mock
   private WorkspaceUsersPresenter workspaceUsersPresenter;
   @Mock
   private TranslatorInteractionService translatorInteractionService;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      service = new UserSessionService(eventBus, distinctColor, workspaceUsersPresenter, translatorInteractionService);

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
   public void onTransUnitEditEvent()
   {
      // Given:
      EditorClientId editorClientId = editorClientId();
      UserPanelSessionItem sessionItem = new UserPanelSessionItem(panel, person());
      TransUnit transUnit = TestFixture.makeTransUnit(2);
      service.getUserSessionMap().put(editorClientId, sessionItem);
      when(hasTransUnitData.getEditorClientId()).thenReturn(editorClientId);
      when(hasTransUnitData.getSelectedTransUnit()).thenReturn(transUnit);

      // When:
      service.onTransUnitEdit(new TransUnitEditEvent(hasTransUnitData));

      // Then:
      assertThat(service.getUserSessionMap().get(editorClientId).getSelectedTransUnit(), Matchers.sameInstance(transUnit));
      assertThat(service.getUserSessionMap().get(editorClientId).getSelectedTransUnit(), Matchers.sameInstance(transUnit));
   }

   @Test
   public void onEnterWorkspace()
   {
      EnterWorkspaceEvent event = mock(EnterWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      when(workspaceUsersPresenter.addNewUser(person)).thenReturn(panel);
      when(distinctColor.getOrCreateColor(editorClientId)).thenReturn("red");

      service.onEnterWorkspace(event);

      assertThat(service.getUserSessionMap(), Matchers.hasKey(editorClientId));
      UserPanelSessionItem userPanel = service.getUserSessionMap().get(editorClientId);
      assertThat(userPanel.getPanel(), Matchers.sameInstance(panel));
      verify(panel).setColor("red");
      assertThat(userPanel.getSelectedTransUnit(), Matchers.nullValue());
   }

   @Test
   public void onExitWorkspace()
   {
      ExitWorkspaceEvent event = mock(ExitWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      UserPanelSessionItem sessionItem = new UserPanelSessionItem(panel, person);
      TransUnit selectedTransUnit = TestFixture.makeTransUnit(1);
      sessionItem.setSelectedTransUnit(selectedTransUnit);
      service.getUserSessionMap().put(editorClientId, sessionItem);

      service.onExitWorkspace(event);

      verify(workspaceUsersPresenter).removeUser(panel, person.getId().toString());
      verify(translatorInteractionService).personExit(person, selectedTransUnit);
      assertThat(service.getUserSessionMap().size(), Matchers.is(0));
   }

}
