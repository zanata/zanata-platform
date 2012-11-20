/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class SaveAsApprovedConfirmationPanel extends DecoratedPopupPanel
{
   private Button saveAsApproved = new Button("Save as Approved");
   private Button cancel = new Button("Cancel");
   private CheckBox rememberDecision = new CheckBox("I understand these keys is Save as Approved.  Don't ask me again.");
   private TransUnitId transUnitId;

   private TargetContentsDisplay.Listener listener;

   public SaveAsApprovedConfirmationPanel()
   {
      super(false, true);
      VerticalPanel vp = new VerticalPanel();
      vp.setSpacing(10);
      vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);


      Label message = new Label("Warning! You are saving a Fuzzy translation as Approved without content changes.");
      Label message2 = new Label("Proceed with one of the following actions:");

      FlowPanel panel = new FlowPanel();
      panel.addStyleName("message");

      panel.add(message);
      panel.add(message2);

      InlineLabel info = new InlineLabel();
      info.setStyleName("icon-info-circle-2 infoIcon");
      InlineLabel message3 = new InlineLabel("For navigation only, please use: ");
      Label message4 = new Label("ALT+Up or ALT+J:  Move to previous row");
      Label message5 = new Label("ALT+Down or ALT+K:  Move to next row");
      message4.addStyleName("centerAlign");
      message5.addStyleName("centerAlign");

      FlowPanel panel2 = new FlowPanel();
      panel2.addStyleName("info");

      panel2.add(info);
      panel2.add(message3);
      panel2.add(message4);
      panel2.add(message5);

      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setSpacing(5);
      buttonPanel.setHeight("100%");
      buttonPanel.add(saveAsApproved);
      buttonPanel.add(cancel);
      setStyleName("confirmationDialogPanel");

      vp.add(panel);
      vp.add(panel2);
      vp.add(buttonPanel);
      vp.add(rememberDecision);
      add(vp);

      hide();
   }

   public void setListener(TargetContentsDisplay.Listener listener)
   {
      this.listener = listener;
      addListenerToButtons();
   }

   private void addListenerToButtons()
   {
      saveAsApproved.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.saveAsApprovedAndMoveNext(transUnitId);
            hide();
         }
      });
      cancel.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            hide();
         }
      });
      rememberDecision.addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            listener.saveUserDecision(!event.getValue());
         }
      });
   }
   
   public void center(TransUnitId transUnitId)
   {
      this.transUnitId = transUnitId;
      center();
   }

   public void setRememberUserDecision(boolean value)
   {
      rememberDecision.setValue(value);
   }
}


 