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
import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.presenter.TranslationEditorPresenter;
import org.zanata.webtrans.client.presenter.TranslationPresenter;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationView extends Composite implements TranslationPresenter.Display
{
   interface TranslationViewUiBinder extends UiBinder<LayoutPanel, TranslationView>
   {
   }

   private static TranslationViewUiBinder uiBinder = GWT.create(TranslationViewUiBinder.class);

   @UiField(provided = true)
   LayoutPanel southPanelContainer;

   @UiField
   LayoutPanel southPanel;

   @UiField
   LayoutPanel editorContainer;

   @UiField(provided = true)
   SplitLayoutPanel mainSplitPanel;

   SplitLayoutPanel tmGlossaryPanel;

   private static double SOUTH_PANEL_HEIGHT = 150;
   private static double MIN_SOUTH_PANEL_HEIGHT = 0;
   private final static double GLOSSARY_PANEL_WIDTH = 500;

   private final static int ANIMATE_DURATION = 200;

   @Inject
   public TranslationView(Resources resources, WebTransMessages messages, TranslationEditorPresenter.Display translationEditorView, TransMemoryPresenter.Display transMemoryView, GlossaryPresenter.Display glossaryView)
   {

      StyleInjector.inject(resources.style().getText(), true);
      southPanelContainer = new LayoutPanel();

      tmGlossaryPanel = new SplitLayoutPanel(3);
      
      mainSplitPanel = new SplitLayoutPanel(3);

      initWidget(uiBinder.createAndBindUi(this));
      mainSplitPanel.setWidgetMinSize(southPanelContainer, (int) MIN_SOUTH_PANEL_HEIGHT);

      southPanel.add(tmGlossaryPanel);

      setEditorView(translationEditorView.asWidget());

      setGlossaryView(glossaryView.asWidget());
      setTranslationMemoryView(transMemoryView.asWidget());
   }

   private void setTranslationMemoryView(Widget translationMemoryView)
   {
      tmGlossaryPanel.remove(translationMemoryView);
      tmGlossaryPanel.add(translationMemoryView);
   }

   private void setGlossaryView(Widget glossaryView)
   {
      tmGlossaryPanel.remove(glossaryView);
      tmGlossaryPanel.addEast(glossaryView, GLOSSARY_PANEL_WIDTH);
   }

   private void setEditorView(Widget editorView)
   {
      this.editorContainer.add(editorView);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }
  
   @Override
   public void setSouthPanelExpanded(boolean expanded)
   {
      mainSplitPanel.forceLayout();
      Widget splitter = SplitLayoutPanelHelper.getAssociatedSplitter(mainSplitPanel, southPanelContainer);
      if (expanded)
      {
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, SOUTH_PANEL_HEIGHT);
      }
      else
      {
         SOUTH_PANEL_HEIGHT = mainSplitPanel.getWidgetContainerElement(southPanelContainer).getOffsetHeight();
         SplitLayoutPanelHelper.setSplitPosition(mainSplitPanel, southPanelContainer, MIN_SOUTH_PANEL_HEIGHT);
      }
      splitter.setVisible(expanded);
      mainSplitPanel.animate(ANIMATE_DURATION);

   }
}
