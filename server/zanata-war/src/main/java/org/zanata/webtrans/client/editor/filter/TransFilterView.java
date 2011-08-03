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
package org.zanata.webtrans.client.editor.filter;

import org.zanata.webtrans.client.Resources;
import org.zanata.webtrans.client.ui.ClearableTextBox;
import org.zanata.webtrans.client.ui.UiMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransFilterView extends Composite implements TransFilterPresenter.Display
{

   private static TransFilterViewUiBinder uiBinder = GWT.create(TransFilterViewUiBinder.class);

   interface TransFilterViewUiBinder extends UiBinder<Widget, TransFilterView>
   {
   }


   @UiField(provided = true)
   ClearableTextBox filterTextBox;

   private final TransFilterMessages messages;

   @Inject
   public TransFilterView(final Resources resources, final TransFilterMessages messages, final UiMessages uiMessages)
   {
      this.messages = messages;
      this.filterTextBox = new ClearableTextBox(resources, uiMessages);
      filterTextBox.setEmptyText(messages.findSourceOrTargetString());
      initWidget(uiBinder.createAndBindUi(this));
      getElement().setId("TransFilterView");
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public TextBox getFilterText()
   {
      return filterTextBox.getTextBox();
   }

}
