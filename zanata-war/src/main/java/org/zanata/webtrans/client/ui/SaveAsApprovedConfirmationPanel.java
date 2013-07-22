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

import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class SaveAsApprovedConfirmationPanel extends PopupPanel implements SaveAsApprovedConfirmationDisplay
{
   private final Button saveAsApproved;
   private final Button cancel;
   private final CheckBox rememberDecision;
   private TransUnitId transUnitId;

   private TargetContentsDisplay.Listener listener;

   @Inject
   public SaveAsApprovedConfirmationPanel(TableEditorMessages messages)
   {
      super(false, true);

      FlowPanel panel = new FlowPanel();
      
      saveAsApproved = new Button(messages.saveAsTranslated());
      cancel = new Button(messages.cancel());
      rememberDecision = new CheckBox(messages.saveAsApprovedDialogRememberDecision());

      Label message = new Label(messages.saveAsTranslatedDialogWarning1());
      message.addStyleName("message");


      InlineLabel info = new InlineLabel();
      info.setStyleName("icon-info-circle-2 infoIcon");
      InlineLabel message3 = new InlineLabel(messages.saveAsApprovedDialogInfo1());
      Label message4 = new Label(messages.saveAsApprovedDialogInfo2());
      Label message5 = new Label(messages.saveAsApprovedDialogInfo3());
      message4.addStyleName("subInfo");
      message5.addStyleName("subInfo");

      FlowPanel infoPanel = new FlowPanel();
      infoPanel.addStyleName("info");

      infoPanel.add(info);
      infoPanel.add(message3);
      infoPanel.add(message4);
      infoPanel.add(message5);

      setStyleName("confirmationDialogPanel");

      panel.add(message);
      panel.add(infoPanel);
      panel.add(saveAsApproved);
      panel.add(cancel);
      panel.add(rememberDecision);
      
      add(panel);

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

   @Override
   public void setShowSaveApprovedWarning(boolean value)
   {
      rememberDecision.setValue(!value);
   }
}


 