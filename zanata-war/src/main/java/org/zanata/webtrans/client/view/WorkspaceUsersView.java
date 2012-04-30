package org.zanata.webtrans.client.view;

import java.util.HashMap;

import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.ui.UserPanel;
import org.zanata.webtrans.shared.auth.SessionId;
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

   private final HashMap<Person, UserPanel> userPanelMap;

   public WorkspaceUsersView()
   {
      initWidget(uiBinder.createAndBindUi(this));
      userPanelMap = new HashMap<Person, UserPanel>();
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void addUser(SessionId sessionId, Person person)
   {
      if (!userPanelMap.containsKey(person))
      {
         userPanelMap.put(person, new UserPanel(person.getId().getId(), person.getName(), person.getAvatarUrl()));
      }
      else
      {
         userPanelMap.get(person).addSession();
      }
      updateView();
   }

   @Override
   public void removeUser(Person person)
   {
      if (userPanelMap.containsKey(person))
      {
         if (userPanelMap.get(person).isSingleSession())
         {
            userPanelMap.remove(person);
         }
         else
         {
            userPanelMap.get(person).removeSession();
         }
      }
      updateView();
   }

   private void updateView()
   {
      userListPanel.clear();
      for (UserPanel userPanel : userPanelMap.values())
      {
         userListPanel.add(userPanel);
      }
   }

   @Override
   public int getUserSize()
   {
      return userPanelMap.size();
   }
}
