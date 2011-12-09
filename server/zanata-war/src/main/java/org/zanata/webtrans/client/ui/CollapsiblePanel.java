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


import org.zanata.webtrans.client.editor.table.TableResources;
import org.zanata.webtrans.client.resources.NavigationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;

public class CollapsiblePanel extends Composite
{

   private static CollapsiblePanelUiBinder uiBinder = GWT.create(CollapsiblePanelUiBinder.class);

   interface CollapsiblePanelUiBinder extends UiBinder<Widget, CollapsiblePanel>
   {
   }

   @UiField
   Label headerLabel;

   @UiField
   LayoutPanel contentPanel;

   @UiField(provided = true)
   TableResources resources;

   private final NavigationMessages messages;

   private State currentState = State.IS_HIDDEN;

   enum State
   {
      IS_HIDDEN, IS_SHOWN;
   }

   public CollapsiblePanel(final TableResources resources, final NavigationMessages messages)
   {
      this.resources = resources;
      this.messages = messages;

      initWidget(uiBinder.createAndBindUi(this));
      expand();
   }

   public void setHeader(String header)
   {
      headerLabel.setText(header);
   }

   public void setContent(Widget content)
   {
      contentPanel.clear();
      contentPanel.add(content);
   }

   @UiHandler("headerLabel")
   public void onHeaderLabelClick(ClickEvent event)
   {

      if (currentState == State.IS_HIDDEN)
      {
         expand();
      }
      else if (currentState == State.IS_SHOWN)
      {
         collapse();
      }
   }

   public void expand()
   {
      currentState = State.IS_SHOWN;
      contentPanel.setHeight("100px");
   }

   public void collapse()
   {
      currentState = State.IS_HIDDEN;
      contentPanel.setHeight("0px");
   }

}
