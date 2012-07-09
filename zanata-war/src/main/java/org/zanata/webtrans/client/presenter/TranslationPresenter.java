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

import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NativeEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;


import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;


public class TranslationPresenter extends WidgetPresenter<TranslationPresenter.Display> implements HasLayoutSelection
{
   public interface Display extends WidgetDisplay
   {
      void setParticipantsTitle(String title);

      /**
       * expand to previous size or collapse to show just tabs on the south
       * panel
       * 
       * @param expanded
       */
      void setSouthPanelExpanded(boolean expanded);

      /**
       * Show or completely hide the south panel. The panel will be
       * expanded(false) when made visible after being hidden, even if it was
       * expanded(true) when it was hidden.
       * 
       * @param visible
       */
      void setSouthPanelVisible(boolean visible);

      void setSidePanelVisible(boolean visible);

      HasValue<Boolean> getOptionsToggle();

      void setOptionsToggleTooltip(String tooltip);

      HasValue<Boolean> getSouthPanelToggle();

      boolean isUserPanelOpen();

      HasSelectionHandlers<Integer> getSouthTabPanel();

      void startAlert(int periodMillis);

      void cancelAlert();
   }

   private final DispatchAsync dispatcher;

   private final TranslationEditorPresenter translationEditorPresenter;
   private final OptionsPanelPresenter optionsPanelPresenter;
   private final TransMemoryPresenter transMemoryPresenter;
   private final GlossaryPresenter glossaryPresenter;
   private final WorkspaceUsersPresenter workspaceUsersPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final KeyShortcutPresenter keyShortcutPresenter;

   private WorkspaceContext workspaceContext;

   private final WebTransMessages messages;

   private boolean southPanelExpanded = true;

   @Inject
   public TranslationPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, final TargetContentsPresenter targetContentsPresenter, final WorkspaceUsersPresenter workspaceUsersPresenter, final TranslationEditorPresenter translationEditorPresenter, final OptionsPanelPresenter optionsPanelPresenter, final TransMemoryPresenter transMemoryPresenter, final GlossaryPresenter glossaryPresenter, final WebTransMessages messages, final NativeEvent nativeEvent, final WorkspaceContext workspaceContext, final KeyShortcutPresenter keyShortcutPresenter)
   {
      super(display, eventBus);
      this.messages = messages;
      this.translationEditorPresenter = translationEditorPresenter;
      this.workspaceUsersPresenter = workspaceUsersPresenter;
      this.transMemoryPresenter = transMemoryPresenter;
      this.optionsPanelPresenter = optionsPanelPresenter;
      this.glossaryPresenter = glossaryPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.dispatcher = dispatcher;

      this.workspaceContext = workspaceContext;
   }

   @Override
   public void onRevealDisplay()
   {
      targetContentsPresenter.concealDisplay();
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
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
            workspaceUsersPresenter.initUserList(result.getTranslatorList());
            display.setParticipantsTitle(messages.nUsersOnline(result.getSize()));
         }
      });
   }

   @Override
   protected void onBind()
   {
      bindSouthPanelPresenters();
      translationEditorPresenter.bind();
      optionsPanelPresenter.bind();


      registerHandler(eventBus.addHandler(ExitWorkspaceEvent.getType(), new ExitWorkspaceEventHandler()
      {
         @Override
         public void onExitWorkspace(ExitWorkspaceEvent event)
         {
            workspaceUsersPresenter.removeTranslator(event.getEditorClientId(), event.getPerson());
            targetContentsPresenter.updateTranslators();
            display.setParticipantsTitle(messages.nUsersOnline(workspaceUsersPresenter.getTranslatorsSize()));
         }
      }));

      registerHandler(eventBus.addHandler(EnterWorkspaceEvent.getType(), new EnterWorkspaceEventHandler()
      {
         @Override
         public void onEnterWorkspace(EnterWorkspaceEvent event)
         {
            workspaceUsersPresenter.addTranslator(event.getEditorClientId(), event.getPerson(), null);
            workspaceUsersPresenter.dispatchChatAction(null, messages.hasJoinedWorkspace(event.getPerson().getId().toString()), MESSAGE_TYPE.SYSTEM_MSG);
            display.setParticipantsTitle(messages.nUsersOnline(workspaceUsersPresenter.getTranslatorsSize()));
         }
      }));

      // We won't receive the EnterWorkspaceEvent generated by our own login,
      // because this presenter is not bound until we get the callback from
      // EventProcessor.
      // Thus we load the translator list here.
      loadTranslatorList();

      registerHandler(eventBus.addHandler(PublishWorkspaceChatEvent.getType(), new PublishWorkspaceChatEventHandler()
      {
         @Override
         public void onPublishWorkspaceChat(PublishWorkspaceChatEvent event)
         {
            if (!display.isUserPanelOpen())
            {
               display.setParticipantsTitle(messages.nUsersOnline(workspaceUsersPresenter.getTranslatorsSize()) + " *");
               display.startAlert(800);
            }
         }
      }));

      registerHandler(display.getSouthTabPanel().addSelectionHandler(new SelectionHandler<Integer>()
      {

         @Override
         public void onSelection(SelectionEvent<Integer> event)
         {
            display.setParticipantsTitle(messages.nUsersOnline(workspaceUsersPresenter.getTranslatorsSize()));
            display.cancelAlert();
         }
      }));

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            setSouthPanelReadOnly(event.isReadOnly());
         }
      }));

      if (workspaceContext.isReadOnly())
      {
         setSouthPanelReadOnly(true);
      }

      registerHandler(display.getOptionsToggle().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {

         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            boolean shouldShowOptions = event.getValue();
            setOptionsExpended(shouldShowOptions);
         }
      }));

      registerHandler(display.getSouthPanelToggle().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            boolean shouldShowSouthPanel = event.getValue();
            setSouthPanelExpanded(shouldShowSouthPanel);
         }
      }));

      KeyShortcutEventHandler gotoPreRowHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            translationEditorPresenter.gotoPrevRow(false);
         }
      };

      KeyShortcutEventHandler gotoNextRowHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            translationEditorPresenter.gotoNextRow(false);
         }
      };

      // Register shortcut ALT+(UP/J) for previous row navigation
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP), ShortcutContext.Navigation, messages.navigateToNextRow(), KeyEvent.KEY_DOWN, true, true, gotoPreRowHandler));
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'J'), ShortcutContext.Navigation, messages.navigateToNextRow(), KeyEvent.KEY_DOWN, true, true, gotoPreRowHandler));

      // Register shortcut ALT+(Down/K) for next row navigation
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN), ShortcutContext.Navigation, messages.navigateToPreviousRow(), KeyEvent.KEY_DOWN, true, true, gotoNextRowHandler));
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'K'), ShortcutContext.Navigation, messages.navigateToPreviousRow(), KeyEvent.KEY_DOWN, true, true, gotoNextRowHandler));

      // Register shortcut Enter to open editor in selected row - if no other input field is in focus
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Navigation, messages.openEditorInSelectedRow(), KeyEvent.KEY_UP, true, true, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (!isOtherInputFieldFocused())
            {
               translationEditorPresenter.openEditorOnSelectedRow();
            }
         }
      }));
   }

   private boolean isOtherInputFieldFocused()
   {
      return translationEditorPresenter.isTransFilterFocused() || 
            transMemoryPresenter.getDisplay().isFocused() || 
            glossaryPresenter.getDisplay().isFocused() || 
            translationEditorPresenter.getDisplay().isPagerFocused();
   }

   @Override
   protected void onUnbind()
   {
      unbindSouthPanelPresenters();
      translationEditorPresenter.unbind();
      optionsPanelPresenter.unbind();
   }

   public void saveEditorPendingChange()
   {
      translationEditorPresenter.saveEditorPendingChange();
   }

   /**
    * Handle all changes required to completely hide and unbind the south panel
    * for read-only mode, or to undo said changes.
    * 
    * @param readOnly
    */
   private void setSouthPanelReadOnly(boolean readOnly)
   {
      if (readOnly)
      {
         // includes unbinding
         setSouthPanelExpanded(false);
      }
      display.setSouthPanelVisible(!readOnly);
      if (!readOnly)
      {
         setSouthPanelExpanded(display.getSouthPanelToggle().getValue());
      }
   }

   /**
    * Expand or collapse south panel, binding or unbinding presenters as
    * appropriate. Will have no effect if the panel is already in the state of
    * expansion or contraction that is specified.
    * 
    * @param expanded
    */
   private void setSouthPanelExpanded(boolean expanded)
   {
      if (expanded == southPanelExpanded)
      {
         return; // nothing to do
      }
      display.setSouthPanelExpanded(expanded);
      southPanelExpanded = expanded;
      if (expanded)
      {
         bindSouthPanelPresenters();

         TransUnit tu = translationEditorPresenter.getSelectedTransUnit();
         if (tu != null)
         {
            transMemoryPresenter.createTMRequestForTransUnit(tu);
            glossaryPresenter.createGlossaryRequestForTransUnit(tu);
         }
      }
      else
      {
         unbindSouthPanelPresenters();
      }
   }
   
   private void setOptionsExpended(boolean shouldShowOptions){
      if (shouldShowOptions)
      {
         display.setSidePanelVisible(true);
         display.setOptionsToggleTooltip(messages.hideOptions());
      }
      else
      {
         display.setSidePanelVisible(false);
         display.setOptionsToggleTooltip(messages.showOptions());
      }
   }

   private void bindSouthPanelPresenters()
   {
      transMemoryPresenter.bind();
      glossaryPresenter.bind();
      workspaceUsersPresenter.bind();
   }

   private void unbindSouthPanelPresenters()
   {
      transMemoryPresenter.unbind();
      glossaryPresenter.unbind();
      workspaceUsersPresenter.unbind();
   }

   @Override
   public void setSouthPanelVisible(boolean visible)
   {
      display.getSouthPanelToggle().setValue(visible, true);
   }

   @Override
   public void setSidePanelVisible(boolean visible)
   {
      display.getOptionsToggle().setValue(visible, true);
   }

   public void concealDisplay()
   {
      targetContentsPresenter.concealDisplay();
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, false);
   }

}
