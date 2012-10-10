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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.inject.Inject;


public class TranslationPresenter extends WidgetPresenter<TranslationPresenter.Display> implements WorkspaceContextUpdateEventHandler
{
   public interface Display extends WidgetDisplay
   {
      void setSouthPanelExpanded(boolean expanded);
   }

   private final TranslationEditorPresenter translationEditorPresenter;
   private final TransMemoryPresenter transMemoryPresenter;
   private final GlossaryPresenter glossaryPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final KeyShortcutPresenter keyShortcutPresenter;

   private final UserWorkspaceContext userWorkspaceContext;
   private final NavigationService navigationService;

   private final WebTransMessages messages;

   private boolean southPanelExpanded = true;

   @Inject
   public TranslationPresenter(Display display, EventBus eventBus, TargetContentsPresenter targetContentsPresenter, TranslationEditorPresenter translationEditorPresenter, TransMemoryPresenter transMemoryPresenter, GlossaryPresenter glossaryPresenter, WebTransMessages messages, UserWorkspaceContext userWorkspaceContext, KeyShortcutPresenter keyShortcutPresenter, NavigationService navigationService)
   {
      super(display, eventBus);
      this.messages = messages;
      this.translationEditorPresenter = translationEditorPresenter;
      this.transMemoryPresenter = transMemoryPresenter;
      this.glossaryPresenter = glossaryPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.userWorkspaceContext = userWorkspaceContext;
      this.navigationService = navigationService;
   }

   @Override
   public void onRevealDisplay()
   {
      targetContentsPresenter.concealDisplay();
   }

   @Override
   protected void onBind()
   {
      bindSouthPanelPresenters();
      translationEditorPresenter.bind();

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this));
      setSouthPanelReadOnly(userWorkspaceContext.hasReadOnlyAccess());

      KeyShortcutEventHandler gotoPreRowHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            eventBus.fireEvent(NavTransUnitEvent.PREV_ENTRY_EVENT);
         }
      };

      KeyShortcutEventHandler gotoNextRowHandler = new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
            eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
         }
      };

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(
            new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP), new Keys(Keys.ALT_KEY, 'J')),
            ShortcutContext.Navigation, messages.navigateToPreviousRow(), KeyEvent.KEY_DOWN, true, true, gotoPreRowHandler));

      keyShortcutPresenter.register(new KeyShortcut(Keys.setOf(
            new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN), new Keys(Keys.ALT_KEY, 'K')),
            ShortcutContext.Navigation, messages.navigateToNextRow(), KeyEvent.KEY_DOWN, true, true, gotoNextRowHandler));

      // Register shortcut Enter to open editor in selected row - if no other input field is in focus
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Navigation, messages.openEditorInSelectedRow(), KeyEvent.KEY_UP, true, true, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (!isOtherInputFieldFocused() && userWorkspaceContext.hasWriteAccess())
            {
               targetContentsPresenter.setFocus();
               targetContentsPresenter.revealDisplay();
            }
         }
      }));
   }

   private boolean isOtherInputFieldFocused()
   {
      return translationEditorPresenter.isTransFilterFocused() || 
            transMemoryPresenter.isFocused() || 
            glossaryPresenter.isFocused() || 
            translationEditorPresenter.getDisplay().isPagerFocused();
   }

   @Override
   protected void onUnbind()
   {
      unbindSouthPanelPresenters();
      translationEditorPresenter.unbind();
   }

   public void saveEditorPendingChange()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
   }

   /**
    * Handle all changes required to completely hide and unbind the south panel
    * for read-only mode, or to undo said changes.
    * 
    * @param readOnly read only
    */
   private void setSouthPanelReadOnly(boolean readOnly)
   {
      if (readOnly)
      {
         // includes unbinding
         setSouthPanelExpanded(false);
      }
      else
      {
         setSouthPanelExpanded(true);
      }
   }

   /**
    * Expand or collapse south panel, binding or unbinding presenters as
    * appropriate. Will have no effect if the panel is already in the state of
    * expansion or contraction that is specified.
    * 
    * @param expanded expand
    */
   public void setSouthPanelExpanded(boolean expanded)
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

         TransUnit tu = navigationService.getSelectedOrNull();
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

   private void bindSouthPanelPresenters()
   {
      transMemoryPresenter.bind();
      glossaryPresenter.bind();
   }

   private void unbindSouthPanelPresenters()
   {
      transMemoryPresenter.unbind();
      glossaryPresenter.unbind();
   }

   public void concealDisplay()
   {
      targetContentsPresenter.concealDisplay();
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, false);
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      setSouthPanelReadOnly(userWorkspaceContext.hasReadOnlyAccess());
   }
}
