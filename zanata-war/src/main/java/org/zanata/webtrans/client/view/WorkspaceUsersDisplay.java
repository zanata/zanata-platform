package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface WorkspaceUsersDisplay extends WidgetDisplay {
    HasManageUserPanel addUser(Person person);

    void appendChat(String user, String timestamp, String msg,
            HasWorkspaceChatData.MESSAGE_TYPE messageType);

    void removeUser(HasManageUserPanel userPanel);

    String getChatInputText();

    void setChatInputText(String chatContent);

    void setListener(Listener listener);

    interface Listener {

        void onSendButtonClicked();

        void onChatInputFocused();

        void onChatInputBlur();
    }
}
