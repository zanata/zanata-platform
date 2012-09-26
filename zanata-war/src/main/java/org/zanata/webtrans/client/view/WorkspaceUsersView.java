package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.client.ui.UserPanel;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WorkspaceUsersView extends Composite implements WorkspaceUsersDisplay
{
   private static WorkspaceUsersViewUiBinder uiBinder = GWT.create(WorkspaceUsersViewUiBinder.class);
   private Listener listener;

   @UiField
   VerticalPanel userListPanel;

   @UiField(provided = true)
   SplitLayoutPanel mainPanel;

   @UiField
   VerticalPanel chatRoom;

   @UiField
   TextBox chatInput;

   @UiField
   PushButton sendButton;

   @UiField
   ScrollPanel chatRoomScrollPanel;

   @UiField
   Styles style;

   @Inject
   public WorkspaceUsersView(final UiMessages uiMessages)
   {
      mainPanel = new SplitLayoutPanel(5);
      initWidget(uiBinder.createAndBindUi(this));

      sendButton.setText(uiMessages.sendLabel());
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public HasManageUserPanel addUser(Person person)
   {
      UserPanel userPanel = new UserPanel(person.getName(), person.getAvatarUrl());
      userListPanel.add(userPanel);
      return userPanel;
   }

   @Override
   public void removeUser(HasManageUserPanel userPanel)
   {
      for (int i = 0; i < userListPanel.getWidgetCount(); i++)
      {
         if (userPanel.equals(userListPanel.getWidget(i)))
         {
            userListPanel.remove(i);
         }
      }
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public String getChatInputText()
   {
      return chatInput.getText();
   }

   @Override
   public void setChatInputText(String chatContent)
   {
      chatInput.setText(chatContent);
   }

   @UiHandler("sendButton")
   public void onSendButtonClick(ClickEvent event)
   {
      listener.onSendButtonClicked();
   }

   @UiHandler("chatInput")
   public void onChatInputFocused(FocusEvent event)
   {
      listener.onChatInputFocused();
   }

   @UiHandler("chatInput")
   public void onChatInputBlur(BlurEvent event)
   {
      listener.onChatInputBlur();
   }

   @Override
   public void appendChat(String user, String timestamp, String msg, MESSAGE_TYPE messageType)
   {
      Label timestampLabel = new Label("[" + timestamp + "]");
      timestampLabel.setStylePrimaryName(style.timeStamp());
      Label msgLabel = new Label(msg);
      if (messageType == MESSAGE_TYPE.SYSTEM_MSG)
      {
         msgLabel.setStyleName(style.systemMsg());
      }
      else if (messageType == MESSAGE_TYPE.SYSTEM_WARNING)
      {
         msgLabel.setStyleName(style.systemWarn());
      } 
      else
      {
         msgLabel.setStyleName(style.msg());
      }

      HorizontalPanel hp = new HorizontalPanel();

      if (!Strings.isNullOrEmpty(timestamp))
      {
         hp.add(timestampLabel);
         hp.setCellWidth(timestampLabel, "107px");
      }
      if (!Strings.isNullOrEmpty(user))
      {
         Label userLabel = new Label(user + ":");
         userLabel.setStyleName(style.userName());
         hp.add(userLabel);
      }
      hp.add(msgLabel);

      chatRoom.add(hp);

      chatRoomScrollPanel.scrollToBottom();
   }

   interface WorkspaceUsersViewUiBinder extends UiBinder<SplitLayoutPanel, WorkspaceUsersView>
   {
   }

   interface Styles extends CssResource
   {
      String systemMsg();

      String userName();

      String systemWarn();

      String msg();

      String timeStamp();

      String chatInput();

      String chatRoom();

      String userListTable();

      String mainPanel();

      String chatRoomScrollPanel();

      String sendButton();
   }
}
