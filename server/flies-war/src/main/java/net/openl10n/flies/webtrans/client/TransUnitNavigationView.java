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
package net.openl10n.flies.webtrans.client;

import net.openl10n.flies.webtrans.client.editor.table.NavigationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitNavigationView extends Composite implements TransUnitNavigationPresenter.Display
{

   private static TransUnitNavigationViewUiBinder uiBinder = GWT.create(TransUnitNavigationViewUiBinder.class);

   interface TransUnitNavigationViewUiBinder extends UiBinder<Widget, TransUnitNavigationView>
   {
   }

   @UiField
   Image nextEntry, prevEntry, prevFuzzyOrUntranslated, nextFuzzyOrUntranslated;

   private final NavigationMessages messages;

   @UiField(provided = true)
   Resources resources;

   @Inject
   public TransUnitNavigationView(final NavigationMessages messages, final Resources resources)
   {
      this.resources = resources;
      this.messages = messages;
      initWidget(uiBinder.createAndBindUi(this));

      prevEntry.setTitle(messages.actionToolTip(messages.prevEntry(), messages.prevEntryShortcut()));
      nextEntry.setTitle(messages.actionToolTip(messages.nextEntry(), messages.nextEntryShortcut()));
      prevFuzzyOrUntranslated.setTitle(messages.actionToolTip(messages.prevFuzzyOrUntranslated(), messages.prevFuzzyOrUntranslatedShortcut()));
      nextFuzzyOrUntranslated.setTitle(messages.actionToolTip(messages.nextFuzzyOrUntranslated(), messages.nextFuzzyOrUntranslatedShortcut()));
   }

   @Override
   public HasClickHandlers getPrevEntryButton()
   {
      return prevEntry;
   }

   @Override
   public HasClickHandlers getNextEntryButton()
   {
      return nextEntry;
   }

   @Override
   public HasClickHandlers getPrevFuzzyOrUntranslatedButton()
   {
      return prevFuzzyOrUntranslated;
   }

   @Override
   public HasClickHandlers getNextFuzzyOrUntranslatedButton()
   {
      return nextFuzzyOrUntranslated;
   }


   @Override
   public Widget asWidget()
   {
      return this;
   }

}
