package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
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
   public void clearUserList()
   {
      userListPanel.clear();
   }

   @Override
   public void addUser(String name, String userImgUrl)
   {
      HorizontalPanel userPanel = new HorizontalPanel();
      Image userImage = new Image(userImgUrl);
      userPanel.add(userImage);
      userPanel.setCellWidth(userImage, "16px");
      userPanel.add(new Label(name));
      
      userListPanel.add(userPanel);
   }
}
