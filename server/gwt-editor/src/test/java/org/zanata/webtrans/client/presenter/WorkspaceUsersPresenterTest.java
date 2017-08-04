package org.zanata.webtrans.client.presenter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.shared.ui.HasManageUserPanel;
import org.zanata.webtrans.test.GWTTestData;
import org.zanata.webtrans.client.view.WorkspaceUsersDisplay;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import com.google.gwt.event.dom.client.KeyCodes;

import net.customware.gwt.presenter.client.EventBus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class WorkspaceUsersPresenterTest {
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
    private KeyShortcutPresenter keyShortcutPresenter;
    @Captor
    private ArgumentCaptor<KeyShortcut> keyShortcutCaptor;
    @Mock
    private HasManageUserPanel userSessionPanel;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        presenter =
                new WorkspaceUsersPresenter(display, eventBus, identity,
                        dispatcher, messages, keyShortcutPresenter);
        verify(display).setListener(presenter);
    }

    @Test
    public void onBind() {
        // ArgumentCaptor<KeyShortcutEventHandler> keyShortcutEventHandlerCaptor
        // = ArgumentCaptor.forClass(KeyShortcutEventHandler.class);
        when(messages.thisIsAPublicChannel()).thenReturn(
                "This is a public channel");
        presenter.onBind();

        verify(keyShortcutPresenter).register(keyShortcutCaptor.capture());
        verify(eventBus).addHandler(PublishWorkspaceChatEvent.getType(),
                presenter);
        verify(display).appendChat(null, null,
                "This is a public channel",
                HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_WARNING);
    }

    @Test
    public void testKeyShortcut() {
        Person person = GWTTestData.person();
        WorkspaceUsersPresenter spyPresenter = Mockito.spy(presenter);
        doNothing().when(spyPresenter).dispatchChatAction(
                person.getId().toString(), "hello",
                HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
        when(messages.publishChatContent()).thenReturn("publish chat");
        spyPresenter.onBind();
        verify(keyShortcutPresenter).register(keyShortcutCaptor.capture());

        // key is 'enter', context is Chat, description is publish chat
        KeyShortcut keyShortcut = keyShortcutCaptor.getValue();
        assertThat(keyShortcut.getAllKeys()).hasSize(1);
        Keys keys = keyShortcut.getAllKeys().iterator().next();
        assertThat(keys.getModifiers()).isEqualTo(Keys.NO_MODIFIER);
        assertThat(keys.getKeyCode()).isEqualTo(KeyCodes.KEY_ENTER);
        assertThat(keyShortcut.getDescription()).isEqualTo("publish chat");
        assertThat(keyShortcut.getContext()).isEqualTo(ShortcutContext.Chat);

        // key handler
        when(identity.getPerson()).thenReturn(person);
        when(display.getChatInputText()).thenReturn("hello");
        keyShortcut.getHandler().onKeyShortcut(new KeyShortcutEvent(keys));
        verify(spyPresenter).dispatchChatAction(person.getId().toString(),
                "hello", HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onSendButtonClicked() {
        // Given:
        when(display.getChatInputText()).thenReturn("hello");
        Person person = GWTTestData.person();
        when(identity.getPerson()).thenReturn(person);

        // When:
        presenter.onSendButtonClicked();

        // Then:
        ArgumentCaptor<PublishWorkspaceChatAction> actionCaptor =
                ArgumentCaptor.forClass(PublishWorkspaceChatAction.class);
        verify(dispatcher).execute(actionCaptor.capture(),
                isA(NoOpAsyncCallback.class));
        PublishWorkspaceChatAction chatAction = actionCaptor.getValue();
        assertThat(chatAction.getPerson()).isEqualTo(person.getId().toString());
        assertThat(chatAction.getMsg()).isEqualTo("hello");
        assertThat(chatAction.getMessageType())
                .isEqualTo(HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
        verify(display).setChatInputText("");
    }

    @Test
    public void onChatInputFocus() {
        presenter.onChatInputFocused();

        verify(keyShortcutPresenter).setContextActive(ShortcutContext.Chat,
                true);
        verify(keyShortcutPresenter).setContextActive(
                ShortcutContext.Navigation, false);
        verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit,
                false);
    }

    @Test
    public void onChatInputBlur() {
        presenter.onChatInputBlur();

        verify(keyShortcutPresenter).setContextActive(ShortcutContext.Chat,
                false);
        verify(keyShortcutPresenter).setContextActive(
                ShortcutContext.Navigation, true);
    }

    @Test
    public void onPublishWorkspaceChat() {
        PublishWorkspaceChatEvent event =
                Mockito.mock(PublishWorkspaceChatEvent.class);
        when(event.getPersonId()).thenReturn("admin");
        when(event.getMsg()).thenReturn("bye");
        when(event.getMessageType()).thenReturn(
                HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);
        when(event.getTimestamp()).thenReturn("a minute ago");

        presenter.onPublishWorkspaceChat(event);

        verify(display).appendChat(event.getPersonId(), event.getTimestamp(),
                event.getMsg(), event.getMessageType());
    }

    @Test
    public void onAddNewUser() {
        Person person = GWTTestData.person();
        when(messages.hasJoinedWorkspace(person.getId().toString()))
                .thenReturn("someone entered");
        WorkspaceUsersPresenter spyPresenter = spy(presenter);
        doNothing().when(spyPresenter)
                .dispatchChatAction(null, "someone entered",
                        HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);

        spyPresenter.addNewUser(person);

        verify(spyPresenter).dispatchChatAction(null, "someone entered",
                HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);
        verify(display).addUser(person);
    }

    @Test
    public void onRemoveUser() {
        Person person = GWTTestData.person();
        when(messages.hasQuitWorkspace(person.getId().toString())).thenReturn(
                "someone quit");
        WorkspaceUsersPresenter spyPresenter = spy(presenter);
        doNothing().when(spyPresenter).dispatchChatAction(null, "someone quit",
                HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);

        spyPresenter.removeUser(userSessionPanel, person.getId().toString());

        verify(spyPresenter).dispatchChatAction(null, "someone quit",
                HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);
        verify(display).removeUser(userSessionPanel);
    }

    @Test
    public void willIgnoreIfMessageIsBlank() {
        presenter.dispatchChatAction("someone", "",
                HasWorkspaceChatData.MESSAGE_TYPE.USER_MSG);

        verifyZeroInteractions(dispatcher, eventBus, display);
    }
}
