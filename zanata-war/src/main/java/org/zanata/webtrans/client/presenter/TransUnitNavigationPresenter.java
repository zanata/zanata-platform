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
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.view.TransUnitNavigationDisplay;

import com.google.inject.Inject;

public class TransUnitNavigationPresenter extends WidgetPresenter<TransUnitNavigationDisplay> implements TransUnitNavigationDisplay.Listener, UserConfigChangeHandler
{

   private UserConfigHolder configHolder;
   private final TargetContentsPresenter targetContentsPresenter;

   @Inject
   public TransUnitNavigationPresenter(TransUnitNavigationDisplay display, EventBus eventBus, UserConfigHolder configHolder, TargetContentsPresenter targetContentsPresenter)
   {
      super(display, eventBus);
      this.configHolder = configHolder;
      this.targetContentsPresenter = targetContentsPresenter;
      display.setListener(this);
   }

   @Override
   protected void onBind()
   {
      registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   @Override
   public void goToFirstEntry()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      eventBus.fireEvent(new NavTransUnitEvent(NavigationType.FirstEntry));
   }

   @Override
   public void goToLastEntry()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      eventBus.fireEvent(new NavTransUnitEvent(NavigationType.LastEntry));
   }

   @Override
   public void goToPreviousState()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      eventBus.fireEvent(new NavTransUnitEvent(NavigationType.PrevState));
   }

   @Override
   public void goToNextState()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      eventBus.fireEvent(new NavTransUnitEvent(NavigationType.NextState));
   }

   @Override
   public void goToPreviousEntry()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      eventBus.fireEvent(new NavTransUnitEvent(NavigationType.PrevEntry));
   }

   @Override
   public void goToNextEntry()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      eventBus.fireEvent(new NavTransUnitEvent(NavigationType.NextEntry));
   }

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      if (event.getView() == MainView.Editor)
      {
         display.setNavModeTooltip(configHolder.getState().getNavOption());
      }
   }
}
