package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.client.view.WorkspaceUsersDisplay;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;
import org.zanata.webtrans.shared.rpc.NoOpResult;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class WorkspaceUsersPresenter extends
        WidgetPresenter<WorkspaceUsersDisplay> implements
        WorkspaceUsersDisplay.Listener, PublishWorkspaceChatEventHandler {
    private final Identity identity;
    private final CachingDispatchAsync dispatcher;
    private final WebTransMessages messages;
    private final KeyShortcutPresenter keyShortcutPresenter;

    @Inject
    public WorkspaceUsersPresenter(WorkspaceUsersDisplay display,
            EventBus eventBus, Identity identity,
            CachingDispatchAsync dispatcher, WebTransMessages messages,
            KeyShortcutPresenter keyShortcutPresenter) {
        super(display, eventBus);
        this.identity = identity;
        this.dispatcher = dispatcher;
        this.messages = messages;
        this.keyShortcutPresenter = keyShortcutPresenter;
        display.setListener(this);
    }

    @Override
    protected void onBind() {
        keyShortcutPresenter.register(KeyShortcut.Builder.builder()
                .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER))
                .setContext(ShortcutContext.Chat)
                .setDescription(messages.publishChatContent())
                .setHandler(event -> dispatchChatAction(identity.getPerson().getId()
                        .toString(), display.getChatInputText(),
                        MESSAGE_TYPE.USER_MSG)).build());

        registerHandler(eventBus.addHandler(
                PublishWorkspaceChatEvent.getType(), this));

        display.appendChat(null, null, messages.thisIsAPublicChannel(),
                MESSAGE_TYPE.SYSTEM_WARNING);
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    public void onRevealDisplay() {
    }

    protected void dispatchChatAction(String person, String msg,
            MESSAGE_TYPE messageType) {
        if (!Strings.isNullOrEmpty(msg)) {
            dispatcher.execute(new PublishWorkspaceChatAction(person, msg,
                    messageType), new NoOpAsyncCallback<>());
            display.setChatInputText("");
        }
    }

    @Override
    public void onSendButtonClicked() {
        dispatchChatAction(identity.getPerson().getId().toString(),
                display.getChatInputText(), MESSAGE_TYPE.USER_MSG);
    }

    @Override
    public void onChatInputFocused() {
        keyShortcutPresenter.setContextActive(ShortcutContext.Chat, true);
        keyShortcutPresenter
                .setContextActive(ShortcutContext.Navigation, false);
        keyShortcutPresenter.setContextActive(ShortcutContext.Edit, false);
    }

    @Override
    public void onChatInputBlur() {
        keyShortcutPresenter.setContextActive(ShortcutContext.Chat, false);
        keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
    }

    @Override
    public void onPublishWorkspaceChat(PublishWorkspaceChatEvent event) {
        display.appendChat(event.getPersonId(), event.getTimestamp(),
                event.getMsg(), event.getMessageType());
    }

    public HasManageUserPanel addNewUser(Person person) {
        dispatchChatAction(null,
                messages.hasJoinedWorkspace(person.getId().toString()),
                MESSAGE_TYPE.SYSTEM_MSG);
        return display.addUser(person);
    }

    public void removeUser(HasManageUserPanel panel, String userId) {
        display.removeUser(panel);
        dispatchChatAction(null, messages.hasQuitWorkspace(userId),
                MESSAGE_TYPE.SYSTEM_MSG);
    }
}
