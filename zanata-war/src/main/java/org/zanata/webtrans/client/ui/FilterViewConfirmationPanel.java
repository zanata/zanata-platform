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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class FilterViewConfirmationPanel extends PopupPanel implements FilterViewConfirmationDisplay
{
   private final Button saveTranslated;
   private final Button saveFuzzy;
   private final Button discardChanges;
   private final Button cancelFilter;

   private Listener listener;

   @Inject
   public FilterViewConfirmationPanel(TableEditorMessages messages)
   {
      super(false, true);
      FlowPanel panel = new FlowPanel();
      
      saveTranslated = new Button(messages.saveAsTranslated());
      saveFuzzy = new Button(messages.saveAsFuzzy());
      discardChanges = new Button(messages.discardChanges());
      cancelFilter = new Button(messages.cancelFilter());

      Label message = new Label(messages.saveChangesConfirmationMessage());
      message.addStyleName("message");

      setStyleName("confirmationDialogPanel");
    
      panel.add(message);
      panel.add(saveTranslated);
      panel.add(saveFuzzy);
      panel.add(discardChanges);
      panel.add(cancelFilter);
      
      add(panel);

      hide();
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
      addListenerToButtons();
   }

   private void addListenerToButtons()
   {
      saveTranslated.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.saveAsTranslatedAndFilter();
         }
      });
      saveFuzzy.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.saveAsFuzzyAndFilter();
         }
      });
      discardChanges.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.discardChangesAndFilter();
         }
      });
      cancelFilter.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.cancelFilter();
         }
      });
   }
}


 