/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import java.util.ArrayList;

import org.zanata.webtrans.client.presenter.SidePanelPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.Person;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanel extends Composite implements SidePanelPresenter.Display
{

   private static SidePanelUiBinder uiBinder = GWT.create(SidePanelUiBinder.class);

   interface SidePanelUiBinder extends UiBinder<LayoutPanel, SidePanel>
   {
   }

   private final LayoutPanel usersPanelContainer;

   private final LayoutPanel transUnitDetailContainer;

   private final LayoutPanel validationDetailContainer;

   private final FlowPanel userListPanel;

   @UiField
   StackLayoutPanel mainPanel;

   private final LayoutPanel rootPanel;

   private final WebTransMessages messages;


   @Inject
   public SidePanel(WebTransMessages messages)
   {
      transUnitDetailContainer = new LayoutPanel();
      usersPanelContainer = new LayoutPanel();
      validationDetailContainer = new LayoutPanel();

      userListPanel = new FlowPanel();

      this.messages = messages;

      rootPanel = uiBinder.createAndBindUi(this);
      initWidget(rootPanel);

      usersPanelContainer.add(userListPanel);

      mainPanel.add(transUnitDetailContainer, messages.transUnitDetailsHeading(), 20);
      mainPanel.add(usersPanelContainer, messages.nUsersOnline(0), 20);
   }


   @Override
   public void setTransUnitDetailView(Widget widget)
   {
      transUnitDetailContainer.clear();
      transUnitDetailContainer.add(widget);
   }

   @Override
   public void setValidationDetailView(Widget widget)
   {
      validationDetailContainer.clear();
      validationDetailContainer.add(widget);
   }

   @Override
   public void updateUserList(ArrayList<Person> userList)
   {
      int existingCount = userListPanel.getWidgetCount();
      for (int i = 0; i < existingCount; i++)
      {
         userListPanel.remove(0);
      }

      for (int i = 0; i < userList.size(); i++)
      {
         UserListItem item = new UserListItem(userList.get(i));
         userListPanel.add(item);
      }

      mainPanel.setHeaderText(1, messages.nUsersOnline(userList.size()));
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

}
