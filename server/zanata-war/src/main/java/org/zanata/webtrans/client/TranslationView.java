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
package org.zanata.webtrans.client;

import org.zanata.webtrans.client.ui.SplitLayoutPanelHelper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
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
   LayoutPanel sidePanelOuterContainer;

   @UiField
   LayoutPanel editorContainer, sidePanelContainer;

   @UiField
   SplitLayoutPanel mainSplitPanel;

   @UiField
   Image sidePanelMinimize;

   @UiField
   Image showSidePanelViewLink;

   final WebTransMessages messages;

   private double panelWidth = 20;

   @Inject
   public TranslationView(Resources resources, WebTransMessages messages)
   {
      this.resources = resources;
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);
      this.sidePanelOuterContainer = new LayoutPanel()
      {
         public void onBrowserEvent(Event event)
         {
            super.onBrowserEvent(event);
            switch (event.getTypeInt())
            {
            case Event.ONMOUSEOVER:
               sidePanelMinimize.setVisible(true);
               break;
            case Event.ONMOUSEOUT:
               sidePanelMinimize.setVisible(false);
               break;
            }
         };
      };
      sidePanelOuterContainer.sinkEvents(Event.ONMOUSEOVER | Event.ONMOUSEOUT);
      initWidget(uiBinder.createAndBindUi(this));

      sidePanelMinimize.setVisible(false);
      mainSplitPanel.setWidgetMinSize(sidePanelOuterContainer, (int) panelWidth);
      showSidePanelViewLink.setTitle(messages.showTranslationDetailsPanel());
   }

   @Override
   public HasClickHandlers getHideSidePanelViewButton()
   {
      return sidePanelMinimize;
   }

   @Override
   public HasClickHandlers getShowSidePanelViewButton()
   {
      return showSidePanelViewLink;
   }

   @Override
   public void setShowSidePanelViewButtonVisible(boolean visible)
   {
      showSidePanelViewLink.setVisible(visible);
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
      mainSplitPanel.animate(500);

   }

}
