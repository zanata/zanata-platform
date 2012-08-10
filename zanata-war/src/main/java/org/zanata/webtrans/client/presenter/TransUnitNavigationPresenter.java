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
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEventHandler;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class TransUnitNavigationPresenter extends WidgetPresenter<TransUnitNavigationPresenter.Display> implements HasNavTransUnitHandlers
{

   private UserConfigHolder configHolder;
   private final TargetContentsPresenter targetContentsPresenter;

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getPrevEntryButton();

      HasClickHandlers getNextEntryButton();

      HasClickHandlers getFirstEntryButton();

      HasClickHandlers getLastEntryButton();

      HasClickHandlers getPrevStateButton();

      HasClickHandlers getNextStateButton();

      void setNavModeTooltip(boolean isButtonFuzzy, boolean isButtonUntranslated);

      void setModalNavVisible(boolean visible);
   }

   @Inject
   public TransUnitNavigationPresenter(Display display, EventBus eventBus, UserConfigHolder configHolder, TargetContentsPresenter targetContentsPresenter)
   {
      super(display, eventBus);
      this.configHolder = configHolder;
      this.targetContentsPresenter = targetContentsPresenter;
   }

   @Override
   protected void onBind()
   {
      display.getPrevEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            fireEvent(new NavTransUnitEvent(NavigationType.PrevEntry));
         }
      });

      display.getNextEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            fireEvent(new NavTransUnitEvent(NavigationType.NextEntry));
         }
      });

      display.getFirstEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            fireEvent(new NavTransUnitEvent(NavigationType.FirstEntry));
         }
      });

      display.getLastEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            fireEvent(new NavTransUnitEvent(NavigationType.LastEntry));
         }
      });

      display.getPrevStateButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            fireEvent(new NavTransUnitEvent(NavigationType.PrevState));
         }
      });

      display.getNextStateButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            fireEvent(new NavTransUnitEvent(NavigationType.NextState));
         }
      });

      registerHandler(eventBus.addHandler(UserConfigChangeEvent.getType(), new UserConfigChangeHandler()
      {
         @Override
         public void onValueChanged(UserConfigChangeEvent event)
         {
            display.setNavModeTooltip(configHolder.isButtonFuzzy(), configHolder.isButtonUntranslated());
         }
      }));

      registerHandler(eventBus.addHandler(EnableModalNavigationEvent.getType(), new EnableModalNavigationEventHandler()
      {
         @Override
         public void onEnable(EnableModalNavigationEvent event)
         {
            display.setModalNavVisible(event.isEnable());
         }
      }));

   }

   @Override
   public HandlerRegistration addNavTransUnitHandler(NavTransUnitHandler handler)
   {
      return eventBus.addHandler(NavTransUnitEvent.getType(), handler);
   }

   @Override
   public void fireEvent(GwtEvent<?> event)
   {
      eventBus.fireEvent(event);
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
