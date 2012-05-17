/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.webtrans.client.ui;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class PrefillForm extends Composite
{
   private PrefillPopupPanelDisplay.Listener listener;

   interface PrefillFormUiBinder extends UiBinder<VerticalPanel, PrefillForm>
   {
   }
   private static PrefillFormUiBinder uiBinder = GWT.create(PrefillFormUiBinder.class);

   @UiField
   ListBox approvedPercent;
   @UiField
   Button confirmButton;

   public PrefillForm()
   {
      initWidget(uiBinder.createAndBindUi(this));
   }

   public String getSelectedApprovedPercent()
   {
      return approvedPercent.getValue(approvedPercent.getSelectedIndex());
   }

   public void setListener(PrefillPopupPanelDisplay.Listener listener)
   {
      this.listener = listener;
   }

   @UiHandler("confirmButton")
   public void onConfirmButtonClick(ClickEvent event)
   {
      Preconditions.checkNotNull(listener, "listener cannot be null");
      listener.proceedToPrefill(getSelectedApprovedPercent());
   }
}