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
package org.zanata.webtrans.client.view;


import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DocumentListOptionsView extends Composite implements DocumentListOptionsDisplay
{
   interface DocumentListOptionsUiBinder extends UiBinder<VerticalPanel, DocumentListOptionsView>
   {
   }

   private static DocumentListOptionsUiBinder uiBinder = GWT.create(DocumentListOptionsUiBinder.class);

   @UiField
   Label pageSizeHeader;

   @UiField
   InlineLabel twentyFiveDoc, fiftyDoc, hundredDoc, twoHundredFiftyDoc;

   @UiField
   Styles style;

   private Listener listener;

   interface Styles extends CssResource
   {
      String selectedPageSize();
   }

   @Inject
   public DocumentListOptionsView(WebTransMessages messages)
   {
      initWidget(uiBinder.createAndBindUi(this));
      pageSizeHeader.setText(messages.pageSize());
      twentyFiveDoc.setText("25");
      fiftyDoc.setText("50");
      hundredDoc.setText("100");
      twoHundredFiftyDoc.setText("250");
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @UiHandler("twentyFiveDoc")
   public void onTwentyFiveDocClicked(ClickEvent event)
   {
      listener.onPageSizeClick(25);
      onPageSizeChanged(twentyFiveDoc, 25);
   }

   @UiHandler("fiftyDoc")
   public void onFiftyDocClicked(ClickEvent event)
   {
      listener.onPageSizeClick(50);
      onPageSizeChanged(fiftyDoc, 50);
   }

   @UiHandler("hundredDoc")
   public void onHundredDocClicked(ClickEvent event)
   {
      listener.onPageSizeClick(100);
      onPageSizeChanged(hundredDoc, 100);
   }

   @UiHandler("twoHundredFiftyDoc")
   public void onTwoHundredFiftyDocClicked(ClickEvent event)
   {
      listener.onPageSizeClick(250);
      onPageSizeChanged(twoHundredFiftyDoc, 250);
   }

   private void onPageSizeChanged(InlineLabel selectedWidget, int pageSize)
   {
      twentyFiveDoc.removeStyleName(style.selectedPageSize());
      fiftyDoc.removeStyleName(style.selectedPageSize());
      hundredDoc.removeStyleName(style.selectedPageSize());
      twoHundredFiftyDoc.removeStyleName(style.selectedPageSize());

      selectedWidget.addStyleName(style.selectedPageSize());
   }

   @Override
   public void setOptionsState(UserConfigHolder.ConfigurationState state)
   {
      onPageSizeChange(state.getDocumentListPageSize());
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   private void onPageSizeChange(int pageSize)
   {
      if (pageSize == 25)
      {
         onTwentyFiveDocClicked(null);
      }
      else if (pageSize == 50)
      {
         onFiftyDocClicked(null);
      }
      else if (pageSize == 100)
      {
         onHundredDocClicked(null);
      }
      else if (pageSize == 250)
      {
         onTwoHundredFiftyDocClicked(null);
      }
   }
}
