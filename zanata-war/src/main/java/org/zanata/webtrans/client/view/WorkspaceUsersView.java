package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.client.ui.UnorderedListWidget;
import org.zanata.webtrans.client.ui.UserPanel;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;

import com.google.common.base.Strings;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WorkspaceUsersView extends Composite implements
        WorkspaceUsersDisplay {
    private static WorkspaceUsersViewUiBinder uiBinder = GWT
            .create(WorkspaceUsersViewUiBinder.class);
    private Listener listener;

    @UiField
    UnorderedListWidget userListPanel;

    @UiField(provided = true)
    SplitLayoutPanel mainPanel;

    @UiField
    UnorderedListWidget chatRoom;

    @UiField
    TextBox chatInput;

    @UiField
    Button sendButton;

    @Inject
    public WorkspaceUsersView(final UiMessages uiMessages) {
        mainPanel = new SplitLayoutPanel(5);
        initWidget(uiBinder.createAndBindUi(this));

        sendButton.setText(uiMessages.sendLabel());
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public HasManageUserPanel addUser(Person person) {
        UserPanel userPanel =
                new UserPanel(person.getName(), person.getAvatarUrl());
        userListPanel.add(userPanel);
        return userPanel;
    }

    @Override
    public void removeUser(HasManageUserPanel userPanel) {
        for (int i = 0; i < userListPanel.getWidgetCount(); i++) {
            if (userPanel.equals(userListPanel.getWidget(i))) {
                userListPanel.remove(i);
            }
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public String getChatInputText() {
        return chatInput.getText();
    }

    @Override
    public void setChatInputText(String chatContent) {
        chatInput.setText(chatContent);
    }

    @UiHandler("sendButton")
    public void onSendButtonClick(ClickEvent event) {
        listener.onSendButtonClicked();
    }

    @UiHandler("chatInput")
    public void onChatInputFocused(FocusEvent event) {
        listener.onChatInputFocused();
    }

    @UiHandler("chatInput")
    public void onChatInputBlur(BlurEvent event) {
        listener.onChatInputBlur();
    }

    @Override
    public void appendChat(String user, String timestamp, String msg,
            MESSAGE_TYPE messageType) {

        SafeHtmlBuilder safeHtmlBuilder = new SafeHtmlBuilder();

        StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(timestamp)) {
            sb.append("[").append(timestamp).append("] ");
        }
        if (!Strings.isNullOrEmpty(user)) {
            sb.append(user).append(":");
        }

        if (!sb.toString().isEmpty()) {
            safeHtmlBuilder.appendHtmlConstant("<span class='txt--meta'>");
            safeHtmlBuilder.appendEscaped(sb.toString());
            safeHtmlBuilder.appendHtmlConstant("</span>");
        }

        safeHtmlBuilder.appendHtmlConstant("<span class='"
                + getCssClass(messageType) + "'>");
        safeHtmlBuilder.appendEscaped(msg);
        safeHtmlBuilder.appendHtmlConstant("</span>");

        chatRoom.add(new HTMLPanel("li", safeHtmlBuilder.toSafeHtml()
                .asString()));
    }

    private String getCssClass(MESSAGE_TYPE messageType) {
        if (messageType == MESSAGE_TYPE.SYSTEM_MSG
                || messageType == MESSAGE_TYPE.SYSTEM_WARNING) {
            return "txt--warning";
        }
        return "txt--neutral";
    }

    interface WorkspaceUsersViewUiBinder extends
            UiBinder<SplitLayoutPanel, WorkspaceUsersView> {
    }
}
