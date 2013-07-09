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

import org.zanata.webtrans.client.presenter.UserConfigHolder.ConfigurationState;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.SearchField;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
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

   @UiField
   CheckBox translatedChk, fuzzyChk, untranslatedChk, approvedChk, rejectedChk, hasErrorChk;
   
   @UiField
   CheckBox incompleteChk, completeChk;
   
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
   public void setTranslatedFilter(boolean filterByTranslated)
   {
      translatedChk.setValue(filterByTranslated);
   }

   @Override
   public void setNeedReviewFilter(boolean filterByNeedReview)
   {
      fuzzyChk.setValue(filterByNeedReview);
   }

   @Override
   public void setUntranslatedFilter(boolean filterByUntranslated)
   {
      untranslatedChk.setValue(filterByUntranslated);
   }
   
   @Override
   public void setApprovedFilter(boolean filterByApproved)
   {
      approvedChk.setValue(filterByApproved);
   }
   
   @Override
   public void setRejectedFilter(boolean filterByRejected)
   {
      rejectedChk.setValue(filterByRejected);
   }

   @Override
   public void setHasErrorFilter(boolean filterByHasError)
   {
      hasErrorChk.setValue(filterByHasError);
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
         searchField.setValue("");
         searchField.addStyleName(style.transFilterTextBoxEmpty());
      }
   }

   @UiHandler({"translatedChk", "fuzzyChk", "untranslatedChk", "approvedChk", "rejectedChk", "hasErrorChk"})
   public void onFilterOptionsChanged(ValueChangeEvent<Boolean> event)
   {
      toggleCompleteChk();
      toggleIncompleteChk();
      
      listener.messageFilterOptionChanged(translatedChk.getValue(), fuzzyChk.getValue(), untranslatedChk.getValue(), approvedChk.getValue(), rejectedChk.getValue(), hasErrorChk.getValue());
   }
   
   public void toggleCompleteChk()
   {
      if(translatedChk.getValue() == approvedChk.getValue())
      {
         if(approvedChk.getValue() == true)
         {
            completeChk.setValue(true);
         } 
         else
         {
            completeChk.setValue(false);
         }
      }
      else
      {
         //Should be indeterminate states if all checkboxes has different states, but GWT checkbox doesn't support it
         completeChk.setValue(false);
      }
   }
   
   public void toggleIncompleteChk()
   {
      if(untranslatedChk.getValue() == fuzzyChk.getValue() && fuzzyChk.getValue() == rejectedChk.getValue() && rejectedChk.getValue())
      {
         if(rejectedChk.getValue() == true)
         {
            incompleteChk.setValue(true);
         } 
         else
         {
            incompleteChk.setValue(false);
         }
      }
      else
      {
         //Should be indeterminate states if all checkboxes has different states
         incompleteChk.setValue(false);
      }
   }
   
   @UiHandler("incompleteChk")
   public void onIncompleteChkChanged(ValueChangeEvent<Boolean> event)
   {
      untranslatedChk.setValue(event.getValue());
      fuzzyChk.setValue(event.getValue());
      rejectedChk.setValue(event.getValue());
      onFilterOptionsChanged(event);
   }
   
   @UiHandler("completeChk")
   public void onCompleteChkChanged(ValueChangeEvent<Boolean> event)
   {
      translatedChk.setValue(event.getValue());
      approvedChk.setValue(event.getValue());
      onFilterOptionsChanged(event);
   }

   @Override
   public void setOptionsState(ConfigurationState state)
   {
      translatedChk.setValue(state.isFilterByTranslated());
      fuzzyChk.setValue(state.isFilterByNeedReview());
      untranslatedChk.setValue(state.isFilterByUntranslated());
      approvedChk.setValue(state.isFilterByApproved());
      rejectedChk.setValue(state.isFilterByRejected());
      hasErrorChk.setValue(state.isFilterByHasError());
   }
}
