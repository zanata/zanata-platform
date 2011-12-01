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

import java.util.ArrayList;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SidePanelPresenter extends WidgetPresenter<SidePanelPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setTransUnitDetailView(Widget widget);
      
      void setValidationDetailView(Widget widget);

      void updateUserList(ArrayList<Person> userList);
   }

   private final DispatchAsync dispatcher;
   private final TransUnitDetailsPresenter transUnitDetailsPresenter;
   private final ValidationDetailsPresenter validationDetailsPresenter;
   
   @Inject
   public SidePanelPresenter(final Display display, final EventBus eventBus, CachingDispatchAsync dispatcher, final TransUnitDetailsPresenter transUnitDetailsPresenter, final ValidationDetailsPresenter validationDetailsPresenter, final TransFilterPresenter transFilterPresenter)
   {
      super(display, eventBus);
      this.transUnitDetailsPresenter = transUnitDetailsPresenter;
      this.validationDetailsPresenter = validationDetailsPresenter;
      this.dispatcher = dispatcher;
   }

   @Override
   protected void onBind()
   {
      transUnitDetailsPresenter.bind();
      display.setTransUnitDetailView(transUnitDetailsPresenter.getDisplay().asWidget());

      validationDetailsPresenter.bind();
      display.setValidationDetailView(validationDetailsPresenter.getDisplay().asWidget());

      registerHandler(eventBus.addHandler(ExitWorkspaceEvent.getType(), new ExitWorkspaceEventHandler()
      {
         @Override
         public void onExitWorkspace(ExitWorkspaceEvent event)
         {
            loadTranslatorList();
         }
      }));

      registerHandler(eventBus.addHandler(EnterWorkspaceEvent.getType(), new EnterWorkspaceEventHandler()
      {
         @Override
         public void onEnterWorkspace(EnterWorkspaceEvent event)
         {
            loadTranslatorList();
         }
      }));

      loadTranslatorList();
   }

   private void loadTranslatorList()
   {
      dispatcher.execute(new GetTranslatorList(), new AsyncCallback<GetTranslatorListResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error("error fetching translators list: " + caught.getMessage());
         }

         @Override
         public void onSuccess(GetTranslatorListResult result)
         {
            display.updateUserList(result.getTranslatorList());
         }
      });
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
