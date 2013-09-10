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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.LocaleListBox;
import org.zanata.webtrans.shared.model.Locale;

public class TransUnitChangeSourceLangView extends Composite implements TransUnitChangeSourceLangDisplay, ChangeHandler
{
   private static TransUnitChangeSourceLangViewUiBinder uiBinder = GWT.create(TransUnitChangeSourceLangViewUiBinder.class);
   private Listener listener;
   private UiMessages messages;

   interface Styles extends CssResource
   {
   }
   @UiField
   FlowPanel flowPanel;
   @UiField
   Label descriptionLabel;
   @UiField
   Styles style;
   LocaleListBox sourceLangListBox;

   @Inject
   public TransUnitChangeSourceLangView(final UiMessages messages)
   {
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));

      descriptionLabel.setText(messages.changeSourceLangDescription());

      sourceLangListBox = new LocaleListBox();
      sourceLangListBox.addChangeHandler(this);

      flowPanel.add(sourceLangListBox);

   }

   @Override
   public void buildListBox(List<Locale> locales)
   {
      sourceLangListBox.clear();
      sourceLangListBox.addItem(messages.chooseRefLang(), Locale.notChosenLocale);
      for (Locale locale : locales)
      {
         sourceLangListBox.addItem(locale);
      }
      sourceLangListBox.setSelectedIndex(0);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public void onChange(ChangeEvent event)
   {
      if (sourceLangListBox.getLocaleAtSelectedIndex() == Locale.notChosenLocale)
      {
         listener.onHideReference();
      }
      else
      {
         listener.onShowReference(sourceLangListBox.getLocaleAtSelectedIndex());
      }
   }

   @Override
   public void showReferenceList()
   {
      sourceLangListBox.setVisible(true);
      descriptionLabel.setVisible(true);
   }

   @Override
   public void hideReferenceList()
   {
      sourceLangListBox.setSelectedIndex(0);
      sourceLangListBox.setVisible(false);
      descriptionLabel.setVisible(false);
   }

   interface TransUnitChangeSourceLangViewUiBinder extends UiBinder<Widget, TransUnitChangeSourceLangView>
   {
   }
}
