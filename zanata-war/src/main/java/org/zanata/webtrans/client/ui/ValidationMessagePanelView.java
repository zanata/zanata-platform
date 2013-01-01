/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.zanata.webtrans.client.ui;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.resources.TableEditorMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationMessagePanelView extends Composite implements HasUpdateValidationWarning
{

   private static UI uiBinder = GWT.create(UI.class);

   interface UI extends UiBinder<Widget, ValidationMessagePanelView>
   {
   }

   interface Styles extends CssResource
   {
      String label();

      String content();

      String container();

      String header();
   }

   @UiField
   Label headerLabel;

   @UiField
   VerticalPanel contents;

   @UiField
   Styles style;

   @UiField
   TableEditorMessages messages;
   @UiField
   DisclosurePanel disclosurePanel;

   public ValidationMessagePanelView()
   {
      initWidget(uiBinder.createAndBindUi(this));
      // this is to remove the .header class so that it won't get style from menu.css
      disclosurePanel.getHeader().getParent().removeStyleName("header");
      clear();
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      if (errors == null || errors.isEmpty())
      {
         clear();
         return;
      }
      contents.clear();

      for (String error : errors)
      {
         Label errorLabel = new Label(error);
         errorLabel.addStyleName(style.label());
         contents.add(errorLabel);
      }
      headerLabel.setText(messages.validationWarningsHeading(errors.size()));
      setVisible(true);
   }

   private void clear()
   {
      contents.clear();
      headerLabel.setText(messages.validationWarningsHeading(0));
      setVisible(false);
   }

}
