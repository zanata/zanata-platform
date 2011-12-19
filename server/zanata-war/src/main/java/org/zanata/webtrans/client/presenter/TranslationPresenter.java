/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.CheckKey;
import org.zanata.webtrans.client.editor.CheckKeyImpl;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationPresenter extends WidgetPresenter<TranslationPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void setEditorView(Widget editorView);

      void setSidePanel(Widget sidePanel);

      void setTranslationMemoryView(Widget translationMemoryView);

      void setWorkspaceUsersView(Widget workspaceUsersView);

      void setGlossaryView(Widget glossaryView);

      void setSouthPanelViewVisible(boolean visible);

      HasClickHandlers getHideSouthPanelButton();

      HasClickHandlers getShowSouthPanelButton();

      void setShowSouthPanelButtonVisible(boolean visible);

      void setSidePanelViewVisible(boolean visible);

      void updateWorkspaceUsersTitle(String title);

      ToggleButton getToogleOptionsButton();
   }

   private final DispatchAsync dispatcher;

   private final TranslationEditorPresenter translationEditorPresenter;
   private final SidePanelPresenter sidePanelPresenter;
   private final TransMemoryPresenter transMemoryPresenter;
   private final GlossaryPresenter glossaryPresenter;
   private final WorkspaceUsersPresenter workspaceUsersPresenter;

   private final WebTransMessages messages;

   @Inject
   public TranslationPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, final WorkspaceUsersPresenter workspaceUsersPresenter, final TranslationEditorPresenter translationEditorPresenter, final SidePanelPresenter sidePanelPresenter, final TransMemoryPresenter transMemoryPresenter, final GlossaryPresenter glossaryPresenter, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.messages = messages;
      this.translationEditorPresenter = translationEditorPresenter;
      this.workspaceUsersPresenter = workspaceUsersPresenter;
      this.transMemoryPresenter = transMemoryPresenter;
      this.sidePanelPresenter = sidePanelPresenter;
      this.glossaryPresenter = glossaryPresenter;
      this.dispatcher = dispatcher;
   }

   @Override
   public void onRevealDisplay()
   {
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
            String title = workspaceUsersPresenter.getDisplay().updateUserList(result.getTranslatorList());
            display.updateWorkspaceUsersTitle(title);
         }
      });
   }

   @Override
   protected void onBind()
   {
      transMemoryPresenter.bind();
      display.setTranslationMemoryView(transMemoryPresenter.getDisplay().asWidget());

      workspaceUsersPresenter.bind();
      display.setWorkspaceUsersView(workspaceUsersPresenter.getDisplay().asWidget());

      glossaryPresenter.bind();
      display.setGlossaryView(glossaryPresenter.getDisplay().asWidget());

      translationEditorPresenter.bind();
      display.setEditorView(translationEditorPresenter.getDisplay().asWidget());

      sidePanelPresenter.bind();
      display.setSidePanel(sidePanelPresenter.getDisplay().asWidget());

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

      // We won't receive the EnterWorkspaceEvent generated by our own login,
      // because
      // this presenter is not bound until we get the callback from
      // EventProcessor.
      // Thus we load the translator list here.
      loadTranslatorList();

      registerHandler(display.getToogleOptionsButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (display.getToogleOptionsButton().isDown())
            {
               display.setSidePanelViewVisible(true);
               display.getToogleOptionsButton().setTitle(messages.hideEditorOptions());
               display.getToogleOptionsButton().setText(messages.showEditorOptionsLabel());
            }
            else if (!display.getToogleOptionsButton().isDown())
            {
               display.setSidePanelViewVisible(false);
               display.getToogleOptionsButton().setTitle(messages.showEditorOptions());
               display.getToogleOptionsButton().setText(messages.hideEditorOptionsLabel());
            }
         }
      }));

      registerHandler(display.getHideSouthPanelButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.setSouthPanelViewVisible(false);
            transMemoryPresenter.unbind();
            glossaryPresenter.unbind();
            workspaceUsersPresenter.unbind();
            display.setShowSouthPanelButtonVisible(true);
         }
      }));

      display.setShowSouthPanelButtonVisible(false);
      display.getShowSouthPanelButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            transMemoryPresenter.bind();
            glossaryPresenter.bind();
            workspaceUsersPresenter.bind();

            display.setSouthPanelViewVisible(true);
            display.setShowSouthPanelButtonVisible(false);
            TransUnit tu = translationEditorPresenter.getSelectedTransUnit();
            if (tu != null)
            {
               transMemoryPresenter.showResultsFor(tu);
               // glossaryPresenter.showResultsFor(tu);
            }
         }
      });

      final CheckKey checkKey = new CheckKeyImpl(CheckKeyImpl.Context.Navigation);

      Event.addNativePreviewHandler(new NativePreviewHandler()
      {
         @Override
         public void onPreviewNativeEvent(NativePreviewEvent event)
         {
            /**
             * @formatter:off
             * keyup is used because TargetCellEditor will intercept the event
             * again (Firefox) See textArea.addKeyDownHandler@InlineTargetCellEditor
             * 
             * Only when the Table is showed,editor is closed, search field not
             * focused, the keyboard event will be processed.
             **/
            if (display.asWidget().isVisible() && 
                  !translationEditorPresenter.isTargetCellEditorFocused() && 
                  !translationEditorPresenter.isTransFilterFocused() && 
                  !transMemoryPresenter.getDisplay().isFocused() && 
                  !glossaryPresenter.getDisplay().isFocused() &&
                  !translationEditorPresenter.getDisplay().isPagerFocused())
            {
               //@formatter:on
               checkKey.init(event.getNativeEvent());

               if (event.getNativeEvent().getType().equals("keyup"))
               {
                  if (checkKey.isCopyFromSourceKey())
                  {
                     if (translationEditorPresenter.getSelectedTransUnit() != null)
                     {
                        Log.info("Copy from source");
                        stopDefaultAction(event);
                        translationEditorPresenter.gotoCurrentRow();
                        translationEditorPresenter.cloneAction();
                     }
                  }
                  else if (checkKey.isEnterKey() && !checkKey.isCtrlKey())
                  {
                     if (translationEditorPresenter.getSelectedTransUnit() != null)
                     {
                        if (!translationEditorPresenter.isCancelButtonFocused())
                        {
                           Log.info("open editor");
                           stopDefaultAction(event);
                           translationEditorPresenter.gotoCurrentRow();
                        }
                        translationEditorPresenter.setCancelButtonFocused(false);
                     }
                  }
               }
               if (event.getNativeEvent().getType().equals("keydown"))
               {
                  if (checkKey.isPreviousEntryKey())
                  {
                     Log.info("Go to previous entry");
                     stopDefaultAction(event);
                     translationEditorPresenter.gotoPrevRow(false);
                  }
                  else if (checkKey.isNextEntryKey())
                  {
                     Log.info("Go to next entry");
                     stopDefaultAction(event);
                     translationEditorPresenter.gotoNextRow(false);
                  }
               }
            }
         }

         public void stopDefaultAction(NativePreviewEvent event)
         {
            event.cancel();
            event.getNativeEvent().stopPropagation();
            event.getNativeEvent().preventDefault();
         }
      });

   }

   @Override
   protected void onUnbind()
   {
      transMemoryPresenter.unbind();
      glossaryPresenter.unbind();
      workspaceUsersPresenter.unbind();
      translationEditorPresenter.unbind();
      sidePanelPresenter.unbind();
   }

   public void saveEditorPendingChange()
   {
      translationEditorPresenter.saveEditorPendingChange();
   }

}
