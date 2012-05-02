package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.client.ui.UserPanel;
import org.zanata.webtrans.shared.model.Person;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class WorkspaceUsersView extends Composite implements WorkspaceUsersPresenter.Display
{

   private static WorkspaceUsersViewUiBinder uiBinder = GWT.create(WorkspaceUsersViewUiBinder.class);

   interface WorkspaceUsersViewUiBinder extends UiBinder<LayoutPanel, WorkspaceUsersView>
   {
   }

   @UiField
   VerticalPanel userListPanel;

   public WorkspaceUsersView()
   {
      initWidget(uiBinder.createAndBindUi(this));
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
            break;
         }
      }
   }

   @Override
   public int getUserSize()
   {
      return userListPanel.getWidgetCount();
   }
}
