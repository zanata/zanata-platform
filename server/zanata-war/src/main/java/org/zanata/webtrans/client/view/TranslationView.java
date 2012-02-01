/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.GlossaryPresenter;
import org.zanata.webtrans.client.presenter.SidePanelPresenter;
import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.presenter.TranslationEditorPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.SplitLayoutPanelHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationView extends Composite implements TranslationPresenter.Display
{
   interface TranslationViewUiBinder extends UiBinder<LayoutPanel, TranslationView>
   {
   }

   private static TranslationViewUiBinder uiBinder = GWT.create(TranslationViewUiBinder.class);

   @UiField(provided = true)
   LayoutPanel sidePanelOuterContainer, southPanelContainer;

   @UiField
   TabLayoutPanel southPanelTab;

   @UiField
   LayoutPanel editorContainer, sidePanelContainer;

   @UiField(provided = true)
   ToggleButton toogleOptionsButton;

   @UiField(provided = true)
   ToggleButton toogleSouthButton;

   LayoutPanel tmPanel, userPanel;

   /*
    * TODO: temporary disable glossary functionalities
    */
   // @UiField
   LayoutPanel glossaryPanel;
   private boolean enableGlossary = false;

   @UiField
   SplitLayoutPanel mainSplitPanel;

   final WebTransMessages messages;

   private double panelWidth = 20;
   private double southHeight = 30;


   @Inject
   public TranslationView(Resources resources, WebTransMessages messages, TranslationEditorPresenter.Display translationEditorView, SidePanelPresenter.Display sidePanelView, TransMemoryPresenter.Display transMemoryView, WorkspaceUsersPresenter.Display workspaceUsersView, GlossaryPresenter.Display glossaryView)
   {
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);
      sidePanelOuterContainer = new LayoutPanel();
      southPanelContainer = new LayoutPanel();

      tmPanel = new LayoutPanel();
      userPanel = new LayoutPanel();
      
      toogleOptionsButton = new ToggleButton(messages.hideEditorOptionsLabel(), messages.showEditorOptionsLabel());
      toogleOptionsButton.setTitle(messages.hideEditorOptions());
      toogleOptionsButton.setDown(true);

      toogleSouthButton = new ToggleButton(messages.minimiseLabel(), messages.restoreLabel());

      initWidget(uiBinder.createAndBindUi(this));
      mainSplitPanel.setWidgetMinSize(sidePanelOuterContainer, (int) panelWidth);
      mainSplitPanel.setWidgetMinSize(southPanelContainer, (int) southHeight);


      southPanelTab.add(tmPanel, messages.translationMemoryHeading());
      if (enableGlossary)
      {
         southPanelTab.add(glossaryPanel, "Glossary");
      }
      southPanelTab.add(userPanel, messages.nUsersOnline(0));

      setEditorView(translationEditorView.asWidget());

      setSidePanel(sidePanelView.asWidget());

      setTranslationMemoryView(transMemoryView.asWidget());

      setWorkspaceUsersView(workspaceUsersView.asWidget());

      // TODO glossary temporarily disabled
      if (enableGlossary)
      {
         setGlossaryView(glossaryView.asWidget());
      }


   }

   private void setTranslationMemoryView(Widget translationMemoryView)
   {
      tmPanel.clear();
      tmPanel.add(translationMemoryView);
   }

   private void setWorkspaceUsersView(Widget workspaceUsersView)
   {
      userPanel.clear();
      userPanel.add(workspaceUsersView);
   }

   private void setGlossaryView(Widget glossaryView)
   {
      glossaryPanel.clear();
      glossaryPanel.add(glossaryView);
   }

   private void setEditorView(Widget editorView)
   {
      this.editorContainer.add(editorView);
   }

   private void setSidePanel(Widget sidePanel)
   {
      sidePanelContainer.clear();
      sidePanelContainer.add(sidePanel);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setSidePanelViewVisible(boolean visible)
   {
      mainSplitPanel.forceLayout();
      Widget splitter = SplitLayoutPanelHelper.getAssociatedSplitter(mainSplitPanel, sidePanelOuterContainer);
      if (visible)
      {
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, sidePanelOuterContainer, panelWidth);
      }
      else
      {
         panelWidth = mainSplitPanel.getWidgetContainerElement(sidePanelOuterContainer).getOffsetWidth();
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, sidePanelOuterContainer, 0);
      }
      splitter.setVisible(visible);
      mainSplitPanel.animate(200);

   }

   @Override
   public void setSouthPanelViewVisible(boolean visible)
   {
      mainSplitPanel.forceLayout();
      Widget splitter = SplitLayoutPanelHelper.getAssociatedSplitter(mainSplitPanel, southPanelContainer);
      if (visible)
      {
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, southHeight);
      }
      else
      {
         southHeight = mainSplitPanel.getWidgetContainerElement(southPanelContainer).getOffsetHeight();
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, 30);
      }
      splitter.setVisible(visible);
      mainSplitPanel.animate(200);

   }

   @Override
   public ToggleButton getToogleOptionsButton()
   {
      return toogleOptionsButton;
   }

   @Override
   public ToggleButton getToogleSouthButton()
   {
      return toogleSouthButton;
   }

   @Override
   public void updateWorkspaceUsersTitle(String title)
   {
      southPanelTab.setTabText(1, title);
   }
}
