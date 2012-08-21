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

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.resources.TableEditorMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationMessagePanelView extends Composite implements ValidationMessagePanelDisplay
{

   private static UI uiBinder = GWT.create(UI.class);

   interface UI extends UiBinder<Widget, ValidationMessagePanelView>
   {
   }

   interface Styles extends CssResource
   {
      String label();

      String clickable();

      String content();

      String scrollSection();

      String container();

      String header();
   }

   @UiField
   Label headerLabel;

   @UiField
   FlowPanel contentPanel;

   @UiField
   Anchor runValidationAnchor;

   VerticalPanel contents;

   @UiField
   PushButton validateButton;

   @UiField
   Styles style;

   @UiField
   TableResources images;

   private boolean collapsible;
   private List<String> errors = new ArrayList<String>();
   private final EventBus eventBus;

   @UiField
   TableEditorMessages messages;

   @Inject
   public ValidationMessagePanelView(final EventBus eventBus)
   {
      this.eventBus = eventBus;
      contents = new VerticalPanel();
      initWidget(uiBinder.createAndBindUi(this));
      runValidationAnchor.setText(messages.runValidation());
      setCollapsible(true);
      setHeaderText(messages.validationWarningsHeading(0));
      collapse();
   }

   private void setHeaderText(String header)
   {
      headerLabel.setText(header);
   }

   @Override
   public void clear()
   {
      contentPanel.clear();
      contents.clear();
      collapse();
      setHeaderText(messages.validationWarningsHeading(0));
   }

   @UiHandler("headerLabel")
   public void onHeaderClick(ClickEvent event)
   {
      if (collapsible)
      {
         if (!contentPanel.isVisible())
         {
            expand();
         }
         else if (contentPanel.isVisible())
         {
            collapse();
         }
      }
   }

   @UiHandler("runValidationAnchor")
   public void onClick(ClickEvent event)
   {
      onValidationButtonClick(event);
   }

   @UiHandler("validateButton")
   public void onValidationButtonClick(ClickEvent event)
   {
      eventBus.fireEvent(new RequestValidationEvent());
   }

   public void expand()
   {
      if (contents.getWidgetCount() > 0)
      {
         contentPanel.setHeight("95px");
         contentPanel.setVisible(true);
      }
   }

   public void collapse()
   {
      contentPanel.setHeight("0px");
      contentPanel.setVisible(false);
   }

   public void setCollapsible(boolean collapsible)
   {
      this.collapsible = collapsible;
      if (collapsible)
      {
         headerLabel.setStyleName(style.clickable());
      }
      else
      {
         headerLabel.setStyleName(style.label());
      }
   }

   public List<String> getErrors()
   {
      return errors;
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      this.errors = errors;
      if (errors == null || errors.isEmpty())
      {
         clear();
         return;
      }
      contentPanel.clear();
      contents.clear();

      for (String error : errors)
      {
         contents.add(new Label(error));
      }
      contentPanel.add(contents);
      setHeaderText(messages.validationWarningsHeading(errors.size()));
      expand();
   }

}
