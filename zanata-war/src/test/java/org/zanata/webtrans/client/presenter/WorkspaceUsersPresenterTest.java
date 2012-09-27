package org.zanata.webtrans.client.presenter;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.client.view.WorkspaceUsersDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import com.google.gwt.event.dom.client.KeyCodes;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class WorkspaceUsersPresenterTest
{
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
   @Captor
   private ArgumentCaptor<KeyShortcut> keyShortcutCaptor;
   @Mock
   private UserPanelSessionItem userPanelSessionItem;
   @Mock
   private HasManageUserPanel userSessionPanel;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new WorkspaceUsersPresenter(display, eventBus, identity, dispatcher, messages, sessionService, keyShortcutPresenter);
      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      //      ArgumentCaptor<KeyShortcutEventHandler> keyShortcutEventHandlerCaptor = ArgumentCaptor.forClass(KeyShortcutEventHandler.class);
      when(messages.thisIsAPublicChannel()).thenReturn("Warning! This is a public channel");
      presenter.onBind();

      verify(keyShortcutPresenter).register(keyShortcutCaptor.capture());
      verify(eventBus).addHandler(PublishWorkspaceChatEvent.getType(), presenter);
      verify(eventBus).addHandler(ExitWorkspaceEvent.getType(), presenter);
      verify(eventBus).addHandler(EnterWorkspaceEvent.getType(), presenter);
      verify(display).appendChat(null, null, "Warning! This is a public channel", HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_WARNING);
   }

   @Test
   public void testKeyShortcut()
   {
      Person person = person();
      WorkspaceUsersPresenter spyPresenter = Mockito.spy(presenter);
      doNothing().when(spyPresenter).dispatchChatAction(person.getId().toString(), "hello", HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
      when(messages.publishChatContent()).thenReturn("publish chat");
      spyPresenter.onBind();
      verify(keyShortcutPresenter).register(keyShortcutCaptor.capture());

      // key is 'enter', context is Chat, description is publish chat
      KeyShortcut keyShortcut = keyShortcutCaptor.getValue();
      assertThat(keyShortcut.getAllKeys(), Matchers.hasSize(1));
      Keys keys = keyShortcut.getAllKeys().iterator().next();
      assertThat(keys.getModifiers(), Matchers.equalTo(Keys.NO_MODIFIER));
      assertThat(keys.getKeyCode(), Matchers.equalTo(KeyCodes.KEY_ENTER));
      assertThat(keyShortcut.getDescription(), Matchers.equalTo("publish chat"));
      assertThat(keyShortcut.getContext(), Matchers.equalTo(ShortcutContext.Chat));

      // key handler
      when(identity.getPerson()).thenReturn(person);
      when(display.getChatInputText()).thenReturn("hello");
      keyShortcut.getHandler().onKeyShortcut(new KeyShortcutEvent(keys));
      verify(spyPresenter).dispatchChatAction(person.getId().toString(), "hello", HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
   }

   @Test
   public void onEnterWorkspaceEvent()
   {
      // Given:
      EnterWorkspaceEvent event = mock(EnterWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      when(messages.hasJoinedWorkspace(person.getId().toString())).thenReturn("someone entered");
      when(sessionService.getColor(editorClientId)).thenReturn("red");
      when(sessionService.getUserPanel(editorClientId)).thenReturn(userPanelSessionItem);
      when(userPanelSessionItem.getPanel()).thenReturn(userSessionPanel);

      // When:
      presenter.onEnterWorkspace(event);

      // Then:
      verify(userPanelSessionItem).setSelectedTransUnit(null);
      verify(userSessionPanel).setColor("red");
      verify(sessionService).updateTranslatorStatus(editorClientId, null);
   }

   @Test
   public void onEnterWorkspaceEventWhenUserIsNotThereYet()
   {
      // Given: session service don't have that user yet
      EnterWorkspaceEvent event = mock(EnterWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      when(messages.hasJoinedWorkspace(person.getId().toString())).thenReturn("someone entered");
      when(sessionService.getColor(editorClientId)).thenReturn("red");
      when(sessionService.getUserPanel(editorClientId)).thenReturn(null); // user is not there
      when(display.addUser(person)).thenReturn(userSessionPanel);

      // When:
      presenter.onEnterWorkspace(event);

      // Then:
      verify(sessionService).addUser(eq(editorClientId), isA(UserPanelSessionItem.class));
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
      // Given:
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(messages.hasQuitWorkspace(person.getId().toString())).thenReturn("someone has quit");
      WorkspaceUsersPresenter spyPresenter = Mockito.spy(presenter);
      doNothing().when(spyPresenter).dispatchChatAction(null, "someone has quit", HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);
      ExitWorkspaceEvent event = mock(ExitWorkspaceEvent.class);
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      when(sessionService.getUserPanel(editorClientId)).thenReturn(userPanelSessionItem);
      when(userPanelSessionItem.getPanel()).thenReturn(userSessionPanel);

      // When:
      spyPresenter.onExitWorkspace(event);

      // Then:
      verify(sessionService).removeUser(editorClientId);
      verify(display).removeUser(userSessionPanel);
      verify(spyPresenter).dispatchChatAction(null, "someone has quit", HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);
   }

   @Test
   public void onSendButtonClicked()
   {
      // Given:
      when(display.getChatInputText()).thenReturn("hello");
      Person person = person();
      when(identity.getPerson()).thenReturn(person);

      // When:
      presenter.onSendButtonClicked();

      // Then:
      ArgumentCaptor<PublishWorkspaceChatAction> actionCaptor = ArgumentCaptor.forClass(PublishWorkspaceChatAction.class);
      verify(dispatcher).execute(actionCaptor.capture(), isA(NoOpAsyncCallback.class));
      PublishWorkspaceChatAction chatAction = actionCaptor.getValue();
      assertThat(chatAction.getPerson(), Matchers.equalTo(person.getId().toString()));
      assertThat(chatAction.getMsg(), Matchers.equalTo("hello"));
      assertThat(chatAction.getMessageType(), Matchers.equalTo(HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG));
      verify(display).setChatInputText("");
   }

   @Test
   public void onChatInputFocus()
   {
      presenter.onChatInputFocused();

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Chat, true);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, false);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, false);
   }

   @Test
   public void onChatInputBlur()
   {
      presenter.onChatInputBlur();

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Chat, false);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, true);
   }

   @Test
   public void onPublishWorkspaceChat()
   {
      PublishWorkspaceChatEvent event = Mockito.mock(PublishWorkspaceChatEvent.class);
      when(event.getPersonId()).thenReturn("admin");
      when(event.getMsg()).thenReturn("bye");
      when(event.getMessageType()).thenReturn(HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
      when(event.getTimestamp()).thenReturn("a minute ago");

      presenter.onPublishWorkspaceChat(event);

      verify(display).appendChat(event.getPersonId(), event.getTimestamp(), event.getMsg(), event.getMessageType());
   }
}
