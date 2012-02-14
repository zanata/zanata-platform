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

import org.zanata.webtrans.client.presenter.OptionsPanelPresenter;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OptionsPanelView extends Composite implements OptionsPanelPresenter.Display
{

   private static OptionsPanelUiBinder uiBinder = GWT.create(OptionsPanelUiBinder.class);

   interface OptionsPanelUiBinder extends UiBinder<SplitLayoutPanel, OptionsPanelView>
   {
   }

   @UiField
   SplitLayoutPanel mainPanel;

   @UiField
   LayoutPanel validationOptionsContainer;

   @UiField
   LayoutPanel editorOptionsContainer;



   @Inject
   public OptionsPanelView(WebTransMessages messages, ValidationOptionsPresenter.Display validationOptionsView)
   {
      initWidget(uiBinder.createAndBindUi(this));
      validationOptionsContainer.clear();
      validationOptionsContainer.add(validationOptionsView.asWidget());
   }

   @Override
   public void setEditorOptionsPanel(Widget widget)
   {
      editorOptionsContainer.clear();
      editorOptionsContainer.add(widget);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }




}
