package org.zanata.webtrans.client.presenter;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.view.WorkspaceUsersDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;

import net.customware.gwt.presenter.client.EventBus;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = "unit-tests")
/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class WorkspaceUsersPresenterTest
{
   private final static String PUBLIC_CHANNEL_WARN = "Warning! This is a public channel";
   private WorkspaceUsersPresenter presenter;
   @Mock
   private WorkspaceUsersDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private Identity identity;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private WebTransMessages messages;
   @Mock
   private UserSessionService sessionService;
   @Mock
   private KeyShortcutPresenter keyShortcutPresenter;

   @BeforeMethod
   public void beforeMethod()
   {
      presenter = new WorkspaceUsersPresenter(display, eventBus, identity, dispatcher, messages, sessionService, keyShortcutPresenter);
   }

   @Test
   public void onEnterWorkspaceEvent()
   {
      EnterWorkspaceEvent event = mock(EnterWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      when(messages.hasJoinedWorkspace(person.getId().toString())).thenReturn("someone entered");

      presenter.onEnterWorkspace(event);

//      verify(workspaceUsersPresenter).addTranslator(editorClientId, person, null);
//      verify(workspaceUsersPresenter).dispatchChatAction(null, "someone entered", HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);
   }

   private static Person person()
   {
      return new Person(new PersonId("pid"), "someone", "url");
   }

   private static EditorClientId editorClientId()
   {
      return new EditorClientId("session", 1);
   }

   @Test
   public void onExitWorkspaceEvent()
   {
      ExitWorkspaceEvent event = mock(ExitWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);

      presenter.onExitWorkspace(event);

//      verify(workspaceUsersPresenter).removeTranslator(editorClientId, person);
   }
}
