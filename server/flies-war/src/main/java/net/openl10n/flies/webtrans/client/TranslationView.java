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
package net.openl10n.flies.webtrans.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
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

   @UiField
   LayoutPanel editorContainer, sidePanelContainer, sidePanelOuterContainer;

   @UiField
   SplitLayoutPanel mainSplitPanel;

   final WebTransMessages messages;

   @Inject
   public TranslationView(Resources resources, WebTransMessages messages)
   {
      this.resources = resources;
      this.messages = messages;

      StyleInjector.inject(resources.style().getText(), true);

      initWidget(uiBinder.createAndBindUi(this));
      mainSplitPanel.setWidgetMinSize(sidePanelOuterContainer, 200);

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

}
