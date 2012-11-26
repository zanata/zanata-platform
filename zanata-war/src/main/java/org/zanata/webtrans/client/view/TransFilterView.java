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

import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.SearchField;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransFilterView extends Composite implements TransFilterDisplay
{
   private static TransFilterViewUiBinder uiBinder = GWT.create(TransFilterViewUiBinder.class);

   // TODO deal with showing greyed-out text

   @UiField(provided = true)
   SearchField searchField;

   @UiField
   Styles style;

   private String hintMessage;

   private boolean focused = false;
   private Listener listener;

   interface TransFilterViewUiBinder extends UiBinder<Widget, TransFilterView>
   {
   }

   interface Styles extends CssResource
   {
      String transFilterTextBoxEmpty();
   }

   @Inject
   public TransFilterView(UiMessages messages)
   {
      searchField = new SearchField(this);
      hintMessage = messages.findSourceOrTargetString();
      searchField.setTextBoxTitle(hintMessage);
      initWidget(uiBinder.createAndBindUi(this));
      getElement().setId("TransFilterView");
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public boolean isFocused()
   {
      return focused;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public void setSearchTerm(String searchTerm)
   {
      Log.info("setting search term:[" + searchTerm + "]");
      if (Strings.isNullOrEmpty(searchTerm))
      {
         searchField.setText(hintMessage);
         searchField.addStyleName(style.transFilterTextBoxEmpty());
      }
      else
      {
         searchField.removeStyleName(style.transFilterTextBoxEmpty());
      }
      searchField.setValue(searchTerm);
   }

   @Override
   public void onSearchFieldValueChange(String value)
   {
      listener.searchTerm(value);
   }

   @Override
   public void onSearchFieldBlur()
   {
      focused = false;
      if (searchField.getText().isEmpty())
      {
         searchField.addStyleName(style.transFilterTextBoxEmpty());
         searchField.setText(hintMessage);
      }
   }

   @Override
   public void onSearchFieldFocus()
   {
      focused = true;
   }

   @Override
   public void onSearchFieldClick()
   {
      focused = true;
      if (searchField.containStyleName(style.transFilterTextBoxEmpty()))
      {
         searchField.setText("");
         searchField.removeStyleName(style.transFilterTextBoxEmpty());
      }
   }

   @Override
   public void onSearchFieldCancel()
   {
      if (!searchField.containStyleName(style.transFilterTextBoxEmpty()))
      {
         searchField.setText("");
         searchField.addStyleName(style.transFilterTextBoxEmpty());
      }
   }
}
