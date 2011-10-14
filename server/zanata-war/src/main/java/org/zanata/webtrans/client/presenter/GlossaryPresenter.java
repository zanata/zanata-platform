/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossary.SearchType;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryPresenter extends WidgetPresenter<GlossaryPresenter.Display>
{
   private final WorkspaceContext workspaceContext;
   private final CachingDispatchAsync dispatcher;

   @Inject
   public GlossaryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.workspaceContext = workspaceContext;
   }

   public interface Display extends WidgetDisplay
   {
      HasValue<Boolean> getExactButton();

      HasClickHandlers getSearchButton();

      HasText getGlossaryTextBox();

      void startProcessing();

      void createTable(ArrayList<TranslationMemoryGlossaryItem> memories);
   }

   /*
    * temporary disable glossary functionalities
    */
   boolean disableGlossary = true;

   @Override
   protected void onBind()
   {
      if (!disableGlossary)
      {
         display.getSearchButton().addClickHandler(new ClickHandler()
         {
            @Override
            public void onClick(ClickEvent event)
            {
               String query = display.getGlossaryTextBox().getText();
               GetGlossary.SearchType searchType = display.getExactButton().getValue() ? SearchType.EXACT : SearchType.FUZZY;
               showResults(query, searchType);
            }
         });

         registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), new TransUnitSelectionHandler()
         {
            @Override
            public void onTransUnitSelected(TransUnitSelectionEvent event)
            {
               showResultsFor(event.getSelection());
            }
         }));
      }
   }

   public void showResultsFor(TransUnit transUnit)
   {
      String query = transUnit.getSource();
      display.getGlossaryTextBox().setText("");
      showResults(query, GetGlossary.SearchType.FUZZY);
   }

   private void showResults(String query, GetGlossary.SearchType searchType)
   {
      display.startProcessing();
      final GetGlossary action = new GetGlossary(query, workspaceContext.getWorkspaceId().getLocaleId(), searchType);
      dispatcher.execute(action, new AsyncCallback<GetGlossaryResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error(caught.getMessage(), caught);
         }

         @Override
         public void onSuccess(GetGlossaryResult result)
         {
            ArrayList<TranslationMemoryGlossaryItem> memories = result.getGlossaries();
            display.createTable(memories);
         }
      });
   }


   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }
}


 