/*
 * Copyright (C) 2013 Zanata Project.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package org.zanata.webtrans.client.presenter;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ReferenceVisibleEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.TransUnitChangeSourceLangDisplay;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.rpc.GetLocaleList;
import org.zanata.webtrans.shared.rpc.GetLocaleListResult;

/**
 * 
 * @author Hannes Eskebaek <hannes.eskebaek@databyran.se>
 */
public class TransUnitChangeSourceLangPresenter extends WidgetPresenter<TransUnitChangeSourceLangDisplay> implements TransUnitChangeSourceLangDisplay.Listener
{
   private final CachingDispatchAsync dispatcher;

   private final UserConfigHolder configHolder;

   @Inject
   public TransUnitChangeSourceLangPresenter(TransUnitChangeSourceLangDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, UserConfigHolder configHolder)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
      display.setListener(this);

      setDisplayMode();
   }

   @Override
   protected void onBind()
   {
      buildListBox();
                
      /* TODO: display.setOptionsState(userOptionsService.getConfigHolder().getState());
      if(selectedlocale != noSelection... ){
      transUnitSourceLangPresenter.setSelectedLocale()... */
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

   private void buildListBox()
   {
      GetLocaleList action = new GetLocaleList();

      dispatcher.execute(action, new AsyncCallback<GetLocaleListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, "Failed to fetch locales"));
         }

         @Override
         public void onSuccess(GetLocaleListResult result)
         {
            display.buildListBox(result.getLocales());
         }
      });
   }

   public void setSelectedLocale(Locale locale)
   {
      display.setSelectedLocale(locale);
   }
   
   @Override
   public void onShowReference(Locale selectedLocale)
   {
      eventBus.fireEvent(new ReferenceVisibleEvent(selectedLocale, true));
   }

   @Override
   public void onHideReference()
   {
      eventBus.fireEvent(new ReferenceVisibleEvent(null, false));
   }

   @Override
   public void onSourceLangListBoxOptionChanged(String selectedLocale)
   {
      if(selectedLocale == null){
         selectedLocale = configHolder.DEFAULT_SELECTED_REFERENCE;
      }
      if (!configHolder.getState().getSelectedReferenceForSourceLang().equals(selectedLocale))
      {
         configHolder.setSelectedReferenceForSourceLang(selectedLocale);
      }
      eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
   }
   
   private void setDisplayMode()
   {
      if (configHolder.getState().getSelectedReferenceForSourceLang() != "none")
      {
         //display.showReferenceList();
      }
   }
}
