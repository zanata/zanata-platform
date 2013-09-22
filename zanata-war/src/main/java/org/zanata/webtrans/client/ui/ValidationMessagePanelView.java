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
import java.util.Map;
import java.util.Map.Entry;

import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.shared.model.ValidationAction;
import org.zanata.webtrans.shared.model.ValidationDisplayRules;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationMessagePanelView extends Composite implements HasUpdateValidationMessage
{
   private static UI uiBinder = GWT.create(UI.class);

   interface UI extends UiBinder<Widget, ValidationMessagePanelView>
   {
   }

   interface Styles extends CssResource
   {
      String error();

      String warning();

      String container();

      String header();
   }

   @UiField
   Label headerLabel;

   @UiField
   UnorderedListWidget contents;

   @UiField
   Styles style;

   @UiField
   DisclosurePanel disclosurePanel;

   private TableEditorMessages messages;

   private Map<ValidationAction, List<String>> displayMessages = Maps.newHashMap();

   @Inject
   public ValidationMessagePanelView(TableEditorMessages messages)
   {
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));
      clear();
   }

   private boolean isErrorLocked(ValidationDisplayRules info)
   {
      return info.isEnabled() && info.isLocked();
   }

   @Override
   public void updateValidationMessages(Map<ValidationAction, List<String>> messages)
   {
      if (messages == null || messages.isEmpty())
      {
         clear();
         return;
      }

      this.displayMessages = messages;

      contents.clear();
      int warningCount = 0;
      int errorCount = 0;

      for (Entry<ValidationAction, List<String>> entry : messages.entrySet())
      {
         for (String message : entry.getValue())
         {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.appendEscaped(message);

            HTMLPanel liElement = new HTMLPanel("li", builder.toSafeHtml().asString());
            liElement.setTitle(entry.getKey().getId().getDisplayName());

            if (isErrorLocked(entry.getKey().getRules()))
            {
               liElement.addStyleName(style.error());
               errorCount++;
            }
            else
            {
               liElement.addStyleName(style.warning());
               warningCount++;
            }

            contents.add(liElement);
         }
      }

      headerLabel.setText(this.messages.validationNotificationHeading(warningCount, errorCount));
      setVisible(true);
   }

   private void clear()
   {
      displayMessages.clear();
      contents.clear();
      headerLabel.setText(messages.validationNotificationHeading(0, 0));
      setVisible(false);
   }

   public Map<ValidationAction, List<String>> getErrorMessages()
   {
      Map<ValidationAction, List<String>> errorMessages = Maps.newHashMap();

      for (Entry<ValidationAction, List<String>> entry : displayMessages.entrySet())
      {
         if (isErrorLocked(entry.getKey().getRules()))
         {
            errorMessages.put(entry.getKey(), entry.getValue());
         }
      }
      return errorMessages;
   }

   public void setVisibleIfHasError(boolean visible)
   {
      if(!displayMessages.isEmpty()) // has error message
      {
         setVisible(visible);
      }
      setVisible(false);
   }
}
