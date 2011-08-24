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

import org.zanata.webtrans.client.presenter.SidePanelPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanel extends Composite implements SidePanelPresenter.Display
{

   private static SidePanelUiBinder uiBinder = GWT.create(SidePanelUiBinder.class);

   interface SidePanelUiBinder extends UiBinder<LayoutPanel, SidePanel>
   {
   }

   @UiField(provided = true)
   LayoutPanel usersPanelContainer;

   @UiField
   LayoutPanel transUnitDetailContainer;

   private final int HEIGHT_USERPANEL_EXPANDED = 200;
   private final int HEIGHT_USERPANEL_COLLAPSED = 20;
   private final int USERPANEL_COLLAPSE_DELAY = 1;

   private final LayoutPanel rootPanel;

   private final Timer collapseTimer = new Timer()
   {
      @Override
      public void run()
      {
         collapseUsersPanel();
      }
   };

   private boolean collapseTriggered = false;
   private boolean collapsed = true;

   @Inject
   public SidePanel()
   {
      usersPanelContainer = new LayoutPanel()
      {
         @Override
         public void onBrowserEvent(Event event)
         {
            if (event.getTypeInt() == Event.ONMOUSEOUT)
            {
               if (!collapsed)
               {
                  collapseUsersPanelSoon();
               }
            }
            else if (event.getTypeInt() == Event.ONMOUSEOVER)
            {
               if (collapsed)
               {
                  expandUsersPanel();
               }
               else
               {
                  cancelCollapseUsersPanel();
               }
            }
            super.onBrowserEvent(event);
         }
      };
      rootPanel = uiBinder.createAndBindUi(this);
      initWidget(rootPanel);
      usersPanelContainer.sinkEvents(Event.ONMOUSEOUT | Event.ONMOUSEOVER);
   }


   @Override
   public void setWorkspaceUsersView(Widget widget)
   {
      usersPanelContainer.clear();
      usersPanelContainer.add(widget);
   }

   @Override
   public void setTransUnitDetailView(Widget widget)
   {
      transUnitDetailContainer.clear();
      transUnitDetailContainer.add(widget);
   }

   private void cancelCollapseUsersPanel()
   {
      if (collapseTriggered)
      {
         collapseTimer.cancel();
         collapseTriggered = false;
      }
   }

   private void collapseUsersPanelSoon()
   {
      collapseTriggered = true;
      collapseTimer.schedule(USERPANEL_COLLAPSE_DELAY);
   }

   @Override
   public void collapseUsersPanel()
   {
      if (collapsed)
         return;
      toggleUsersPanel();
   }

   private void toggleUsersPanel()
   {
      rootPanel.forceLayout();
      collapsed = !collapsed;

      int bottomHeight = collapsed ? HEIGHT_USERPANEL_COLLAPSED : HEIGHT_USERPANEL_EXPANDED;
      rootPanel.setWidgetBottomHeight(usersPanelContainer, 0, Unit.PX, bottomHeight, Unit.PX);
      rootPanel.animate(250);
   }

   @Override
   public void expandUsersPanel()
   {
      cancelCollapseUsersPanel();
      if (!collapsed)
         return;
      toggleUsersPanel();
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

}
