package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.client.ui.UserPanel;
import org.zanata.webtrans.shared.model.Person;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class WorkspaceUsersView extends Composite implements WorkspaceUsersPresenter.Display
{

   private static WorkspaceUsersViewUiBinder uiBinder = GWT.create(WorkspaceUsersViewUiBinder.class);

   interface WorkspaceUsersViewUiBinder extends UiBinder<SplitLayoutPanel, WorkspaceUsersView>
   {
   }

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

   @Inject
   public WorkspaceUsersView(final UiMessages uiMessages)
   {
      mainPanel = new SplitLayoutPanel(3);
      initWidget(uiBinder.createAndBindUi(this));
      sendButton.setText(uiMessages.sendLabel());
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public HasManageUserSession addUser(Person person)
   {
      UserPanel userPanel = new UserPanel(person.getName(), person.getAvatarUrl());
      userListPanel.add(userPanel);
      return userPanel;
   }

   @Override
   public void removeUser(HasManageUserSession userPanel)
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
   public HasClickHandlers getSendButton()
   {
      return sendButton;
   }

   @Override
   public HasText getInputText()
   {
      return chatInput;
   }

   @Override
   public void appendChat(String user, String timestamp, String msg)
   {
      if (Strings.isNullOrEmpty(user))
      {
         chatRoom.add(new HTML("[" + timestamp + "]   " + msg));
      }
      else
      {
         chatRoom.add(new HTML("[" + timestamp + "]   " + user + ":  " + msg));
      }

      chatRoomScrollPanel.scrollToBottom();
   }
}
