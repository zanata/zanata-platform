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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ValidationMessagePanel extends Composite
{

   private static ValidationMessagePanelUiBinder uiBinder = GWT.create(ValidationMessagePanelUiBinder.class);

   interface ValidationMessagePanelUiBinder extends UiBinder<Widget, ValidationMessagePanel>
   {
   }

   interface Styles extends CssResource
   {
      String header();

      String header_clickable();
   }

   @UiField
   Label headerLabel;

   @UiField
   VerticalPanel contentPanel;
   
   @UiField
   Styles style;

   private boolean collapsible;
   private List<String> errors = new ArrayList<String>();


   public ValidationMessagePanel(boolean collapsible)
   {
      initWidget(uiBinder.createAndBindUi(this));
      setCollapsible(collapsible);
      collapse();
   }

   public void setHeader(String header)
   {
      headerLabel.setText(header);
   }

   public void clear()
   {
      contentPanel.clear();
      collapse();
   }

   public void setContent(List<String> errors)
   {
      this.errors = errors;
      contentPanel.clear();
      for (String error : errors)
      {
         contentPanel.add(new Label(error));
      }
      expand();
   }

   @UiHandler("headerLabel")
   public void onHeaderLabelClick(ClickEvent event)
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

   public void expand()
   {
      if (contentPanel.getWidgetCount() > 0)
      {
         contentPanel.setHeight("95px");
         contentPanel.setVisible(true);
      }
      else
      {
         collapse();
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
      if(collapsible){
         headerLabel.setStyleName(style.header_clickable());
      }
      else
      {
         headerLabel.setStyleName(style.header());
      }
   }

   public List<String> getErrors()
   {
      return errors;
   }
}
