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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterDisplay> implements TransFilterDisplay.Listener, FindMessageHandler
{

   private final History history;

   @Inject
   public TransFilterPresenter(final TransFilterDisplay display, final EventBus eventBus, final History history)
   {
      super(display, eventBus);
      display.setListener(this);
      this.history = history;
   }

   @Override
   protected void onBind()
   {
      registerHandler(eventBus.addHandler(FindMessageEvent.getType(), this));
   }

   public boolean isFocused()
   {
      return display.isFocused();
   }

   @Override
   public void searchTerm(String searchTerm)
   {
      HistoryToken newToken = history.getHistoryToken();
      newToken.setSearchText(searchTerm);
      history.newItem(newToken);
   }

   @Override
   public void onFindMessage(FindMessageEvent event)
   {
      // this is fired from HistoryEventHandlerService
      display.setSearchTerm(event.getMessage());
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }
}
