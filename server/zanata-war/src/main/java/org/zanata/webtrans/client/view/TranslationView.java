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

import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.SplitLayoutPanelHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationView extends Composite implements TranslationPresenter.Display
{
   interface TranslationViewUiBinder extends UiBinder<LayoutPanel, TranslationView>
   {
   }

   private static TranslationViewUiBinder uiBinder = GWT.create(TranslationViewUiBinder.class);

   @UiField(provided = true)
   final Resources resources;

   @UiField(provided = true)
   LayoutPanel sidePanelOuterContainer, southPanelContainer;

   @UiField
   LayoutPanel editorContainer, sidePanelContainer;

   @UiField
   LayoutPanel tmPanel;

   /*
    * TODO: temporary disable glossary functionalities
    */
   // @UiField
   LayoutPanel glossaryPanel;

   @UiField
   SplitLayoutPanel mainSplitPanel;

   @UiField
   Image sidePanelCollapse;

   @UiField
   Image sidePanelExpend;

   @UiField
   Image tmMinimize;

   @UiField
   Image showTmViewLink;

   final WebTransMessages messages;

   private double panelWidth = 20;
   private double southHeight = 45;

   @Inject
   public TranslationView(Resources resources, WebTransMessages messages)
   {
      this.resources = resources;
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);
      this.sidePanelOuterContainer = new LayoutPanel();
      this.southPanelContainer = new LayoutPanel();

      initWidget(uiBinder.createAndBindUi(this));
      tmMinimize.setVisible(true);
      sidePanelCollapse.setVisible(true);
      mainSplitPanel.setWidgetMinSize(sidePanelOuterContainer, (int) panelWidth);
      mainSplitPanel.setWidgetMinSize(southPanelContainer, (int) southHeight);

      sidePanelExpend.setTitle(messages.showTranslationDetailsPanel());
   }

   @Override
   public void setTranslationMemoryView(Widget translationMemoryView)
   {
      tmPanel.clear();
      tmPanel.add(translationMemoryView);
   }

   /*
    * TODO: temporary disable glossary functionalities
    */
   boolean disableGlossary = true;

   @Override
   public void setGlossaryView(Widget glossaryView)
   {
      if (!disableGlossary)
      {
         glossaryPanel.clear();
         glossaryPanel.add(glossaryView);
      }
   }

   @Override
   public HasClickHandlers getHideSidePanelViewButton()
   {
      return sidePanelCollapse;
   }

   @Override
   public HasClickHandlers getShowSidePanelViewButton()
   {
      return sidePanelExpend;
   }

   @Override
   public void setShowSidePanelViewButtonVisible(boolean visible)
   {
      sidePanelExpend.setVisible(visible);
      sidePanelCollapse.setVisible(!visible);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setEditorView(Widget editorView)
   {
      this.editorContainer.add(editorView);
   }

   @Override
   public void setSidePanel(Widget sidePanel)
   {
      sidePanelContainer.clear();
      sidePanelContainer.add(sidePanel);
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
   public void setTmViewVisible(boolean visible)
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
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, 45);
      }
      splitter.setVisible(visible);
      mainSplitPanel.animate(200);

   }

   @Override
   public HasClickHandlers getHideTMViewButton()
   {
      return tmMinimize;
   }

   @Override
   public HasClickHandlers getShowTMViewButton()
   {
      return showTmViewLink;
   }

   @Override
   public void setShowTMViewButtonVisible(boolean visible)
   {
      showTmViewLink.setVisible(visible);
      tmMinimize.setVisible(!visible);
   }
}
