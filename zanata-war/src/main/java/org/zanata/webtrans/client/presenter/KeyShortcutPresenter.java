/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.inject.Inject;

/**
 * Detects shortcut key combinations such as Alt+KEY and Shift+Alt+KEY and
 * broadcasts corresponding {@link KeyShortcutEvent}s.
 * 
 * Handlers are registered directly with this presenter to avoid excessive
 * traffic on the main event bus and to make handling of events simpler.
 * 
 * Only key-down events are processed.
 * 
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a> *
 */
public class KeyShortcutPresenter extends WidgetPresenter<KeyShortcutPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void addContext(ShortcutContext context, Collection<Set<KeyShortcut>> shorcutSets);

      void showPanel();

      public void clearPanel();

      boolean isShowing();

      void hide(boolean autoClosed);
   }

   /**
    * Key uses {@link KeyShortcut#keysHash()}
    */
   private Map<Integer, Set<KeyShortcut>> shortcutMap;

   private Set<ShortcutContext> activeContexts;

   private WebTransMessages messages;

   @Inject
   public KeyShortcutPresenter(Display display, EventBus eventBus, final WebTransMessages webTransMessages)
   {
      super(display, eventBus);
      this.messages = webTransMessages;
   }

   @Override
   protected void onBind()
   {
      ensureActiveContexts().add(ShortcutContext.Application);

      Event.addNativePreviewHandler(new NativePreviewHandler()
      {

         @Override
         public void onPreviewNativeEvent(NativePreviewEvent event)
         {
            NativeEvent evt = event.getNativeEvent();

            if ((event.getTypeInt() & (Event.ONKEYDOWN | Event.ONKEYUP)) != 0)
            {
               processKeyEvent(evt);
            }
         }
      });

      registerKeyShortcut(new KeyShortcut(0, KeyCodes.KEY_ESCAPE, ShortcutContext.Application, messages.closeShortcutView(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (display.isShowing())
            {
               display.hide(true);
            }
         }
      }, KeyShortcut.KEY_UP_EVENT, true, true, true));

      // could try to use ?, although this is not as simple as passing character
      // '?'
      registerKeyShortcut(new KeyShortcut(KeyShortcut.ALT_KEY, 'Y', ShortcutContext.Application, messages.showAvailableKeyShortcuts(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            display.clearPanel();
            for (ShortcutContext context : ensureActiveContexts())
            {
               display.addContext(context, ensureShortcutMap().values());
            }
            display.showPanel();
         }
      }));
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub
   }

   @Override
   protected void onRevealDisplay()
   {
      // TODO Auto-generated method stub
   }

   public void setContextActive(ShortcutContext context, boolean active)
   {
      if (active)
      {
         ensureActiveContexts().add(context);
      }
      else
      {
         if (context == ShortcutContext.Application)
         {
            // TODO throw exception? Remove this check? Just warn but still
            // remove context?
            Log.warn("Tried to set global shortcut context inactive. Ignoring.");
         }
         else
         {
            ensureActiveContexts().remove(context);
         }
      }
   }

   public HandlerRegistration registerKeyShortcut(KeyShortcut shortcut)
   {
      Log.debug("registering key shortcut. key: " + shortcut.getKeyCode() + " modifier: " + shortcut.getModifiers() + " keyhash: " + shortcut.keysHash());
      Set<KeyShortcut> shortcuts = ensureShortcutMap().get(shortcut.keysHash());
      if (shortcuts == null)
      {
         shortcuts = new HashSet<KeyShortcut>();
         ensureShortcutMap().put(shortcut.keysHash(), shortcuts);
      }
      shortcuts.add(shortcut);
      return new KeyShortcutHandlerRegistration(shortcut);
   }

   /**
    * Process key event - check for 
    * @param evt
    */
   private void processKeyEvent(NativeEvent evt)
   {
      int modifiers = calculateModifiers(evt);
      int keyHash = calculateKeyHash(modifiers, evt.getKeyCode());
      Log.debug("processing key shortcut for key" + evt.getKeyCode() + " with hash " + keyHash);
      Set<KeyShortcut> shortcuts = ensureShortcutMap().get(keyHash);
      if (shortcuts != null)
      {
         KeyShortcutEvent shortcutEvent = new KeyShortcutEvent(modifiers, evt.getKeyCode());
         for (KeyShortcut shortcut : shortcuts)
         {
            if (ensureActiveContexts().contains(shortcut.getContext()) && shortcut.getKeyEvent().equals(evt.getType()))
            {
               if (shortcut.isStopPropagation())
               {
                  evt.stopPropagation();
               }
               if (shortcut.isPreventDefault())
               {
                  evt.preventDefault();
               }
               shortcut.getHandler().onKeyShortcut(shortcutEvent);
            }
         }
      }
      else // this is to check any keyShortcut isNot = true registered
      {
         for (Entry<Integer, Set<KeyShortcut>> entry : ensureShortcutMap().entrySet())
         {
            KeyShortcutEvent shortcutEvent = new KeyShortcutEvent(modifiers, evt.getKeyCode());
            for (KeyShortcut shortcut : entry.getValue())
            {
               if (ensureActiveContexts().contains(shortcut.getContext()) && shortcut.getKeyEvent().equals(evt.getType()) && shortcut.isNot())
               {
                  if (shortcut.isStopPropagation())
                  {
                     evt.stopPropagation();
                  }
                  if (shortcut.isPreventDefault())
                  {
                     evt.preventDefault();
                  }
                  shortcut.getHandler().onKeyShortcut(shortcutEvent);
               }
            }
         }
      }
   }

   /**
    * Calculate a hash that should match {@link KeyShortcut#keysHash()}.
    * 
    * @param evt
    * @return
    * @see KeyShortcut#keysHash()
    */
   private int calculateKeyHash(int modifiers, int keyCode)
   {
      int keyHash = keyCode * 8;
      keyHash |= modifiers;
      return keyHash;
   }

   private int calculateModifiers(NativeEvent evt)
   {
      int modifiers = 0;
      modifiers |= evt.getAltKey() ? KeyShortcut.ALT_KEY : 0;
      modifiers |= evt.getShiftKey() ? KeyShortcut.SHIFT_KEY : 0;
      modifiers |= evt.getCtrlKey() ? KeyShortcut.CTRL_KEY : 0;
      modifiers |= evt.getMetaKey() ? KeyShortcut.META_KEY : 0;
      return modifiers;
   }

   private Set<ShortcutContext> ensureActiveContexts()
   {
      if (activeContexts == null)
      {
         activeContexts = new HashSet<ShortcutContext>();
      }
      return activeContexts;
   }

   private Map<Integer, Set<KeyShortcut>> ensureShortcutMap()
   {
      if (shortcutMap == null)
      {
         shortcutMap = new HashMap<Integer, Set<KeyShortcut>>();
      }
      return shortcutMap;
   }

   private class KeyShortcutHandlerRegistration implements HandlerRegistration
   {

      private KeyShortcut shortcut;

      public KeyShortcutHandlerRegistration(KeyShortcut shortcut)
      {
         this.shortcut = shortcut;
      }

      @Override
      public void removeHandler()
      {
         Set<KeyShortcut> shortcuts = ensureShortcutMap().get(shortcut.keysHash());
         if (shortcuts != null)
         {
            shortcuts.remove(shortcut);
         }
      }

   }
}
