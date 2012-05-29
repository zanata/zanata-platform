/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.LayoutSelectorPresenter;
import org.zanata.webtrans.client.resources.Resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class LayoutSelectorView extends PopupPanel implements LayoutSelectorPresenter.Display
{

   private static final LayoutSelectorUiBinder uiBinder = GWT.create(LayoutSelectorUiBinder.class);

   interface LayoutSelectorUiBinder extends UiBinder<Widget, LayoutSelectorView>
   {
   }

   interface Styles extends CssResource
   {
      String mainPanel();
      
      String layoutTable();

      String optionTD();
   }

   @UiField
   Styles style;

   @UiField
   HorizontalPanel layoutList;


   @UiField
   Resources resources;

   @UiField
   FocusPanel defaultLayoutContainer;
   
   @UiField
   FocusPanel maximizeLayoutContainer;
   
   @UiField
   FocusPanel noOptionLayoutContainer;
   
   @UiField
   FocusPanel noSouthLayoutContainer;
   
   
   private static final String NORTH_WIDTH = "60px";
   private static final String EAST_WIDTH = "25px";
   
   public LayoutSelectorView()
   {
      setWidget(uiBinder.createAndBindUi(this));
      
      setAutoHideEnabled(true);
      setGlassEnabled(true);
      
      setStyleName(style.mainPanel());
      
      defaultLayoutContainer.add(initDefaultLayout());
      maximizeLayoutContainer.add(initMaximiseWorkspaceLayout());
      noOptionLayoutContainer.add(initNoOptionsWorkspaceLayout());
      noSouthLayoutContainer.add(initNoSouthWorkspaceLayout());
   }

   private DockPanel initDefaultLayout()
   {
      DockPanel defaultLayout = new DockPanel();
      defaultLayout.setStyleName(style.layoutTable());
      
      Label optionLabel = new Label("Options");
      Label southLabel = new Label("TM/Glossay/Users");
      Label workspaceLabel = new Label("Workspace");
      
      optionLabel.addStyleName(style.optionTD());

      defaultLayout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      defaultLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      
      defaultLayout.add(southLabel, DockPanel.SOUTH);
      defaultLayout.add(optionLabel, DockPanel.EAST);
      defaultLayout.add(workspaceLabel, DockPanel.CENTER);

      defaultLayout.setCellHeight(workspaceLabel, NORTH_WIDTH);
      defaultLayout.setCellHeight(optionLabel, NORTH_WIDTH);
      defaultLayout.setCellWidth(optionLabel, EAST_WIDTH);
      
      return defaultLayout;
   }

   private DockPanel initMaximiseWorkspaceLayout()
   {
      DockPanel maximizeWorkspaceLayout = new DockPanel();
      maximizeWorkspaceLayout.setStyleName(style.layoutTable());
      
      Label workspaceLabel = new Label("Workspace");
      
      maximizeWorkspaceLayout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      maximizeWorkspaceLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      
      maximizeWorkspaceLayout.add(workspaceLabel, DockPanel.CENTER);
      
      return maximizeWorkspaceLayout;
   }
   
   private DockPanel initNoOptionsWorkspaceLayout()
   {
      DockPanel noOptionsWorkspaceLayout = new DockPanel();
      noOptionsWorkspaceLayout.setStyleName(style.layoutTable());
      
      Label workspaceLabel = new Label("Workspace");
      Label southLabel = new Label("TM/Glossay/Users");

      noOptionsWorkspaceLayout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      noOptionsWorkspaceLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      
      noOptionsWorkspaceLayout.add(workspaceLabel, DockPanel.CENTER);
      noOptionsWorkspaceLayout.add(southLabel, DockPanel.SOUTH);
      
      noOptionsWorkspaceLayout.setCellHeight(workspaceLabel, NORTH_WIDTH);
      
      return noOptionsWorkspaceLayout;
   }
   
   private DockPanel initNoSouthWorkspaceLayout()
   {
      DockPanel noSouthWorkspaceLayout = new DockPanel();
      noSouthWorkspaceLayout.setStyleName(style.layoutTable());
      
      Label workspaceLabel = new Label("Workspace");
      Label optionLabel = new Label("Options");
      optionLabel.addStyleName(style.optionTD());
      
      noSouthWorkspaceLayout.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      noSouthWorkspaceLayout.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
      
      noSouthWorkspaceLayout.add(optionLabel, DockPanel.EAST);
      noSouthWorkspaceLayout.add(workspaceLabel, DockPanel.CENTER);
      
      noSouthWorkspaceLayout.setCellWidth(optionLabel, EAST_WIDTH);
      
      return noSouthWorkspaceLayout;
   }
 
   @Override
   public HasClickHandlers getDefaultLayoutContainer()
   {
      return defaultLayoutContainer;
   }
   
   @Override
   public HasClickHandlers getMaximiseLayoutContainer()
   {
      return maximizeLayoutContainer;
   }
   
   @Override
   public HasClickHandlers getNoOptionLayoutContainer()
   {
      return noOptionLayoutContainer;
   }
   
   @Override
   public HasClickHandlers getNoSouthLayoutContainer()
   {
      return noSouthLayoutContainer;
   }
   
   @Override
   public void show(){
      super.show();
   }
}
