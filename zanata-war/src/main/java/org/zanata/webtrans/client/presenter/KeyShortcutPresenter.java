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
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.SurplusKeyListener;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
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

   private Map<ShortcutContext, Set<SurplusKeyListener>> surplusKeyMap;

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

            // TODO enable keypress events if any shortcuts require them
            if ((event.getTypeInt() & (Event.ONKEYDOWN | Event.ONKEYUP)) != 0)
            {
               processKeyEvent(evt);
            }
         }
      });

      register(new KeyShortcut(KeyShortcut.NO_MODIFIER, KeyCodes.KEY_ESCAPE, ShortcutContext.Application, messages.closeShortcutView(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (display.isShowing())
            {
               display.hide(true);
            }
         }
      }, KeyEvent.KEY_UP, true, true));

      // could try to use ?, although this is not as simple as passing character
      // '?'
      register(new KeyShortcut(KeyShortcut.ALT_KEY, 'Y', ShortcutContext.Application, messages.showAvailableKeyShortcuts(), new KeyShortcutEventHandler()
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

   /**
    * Register a {@link KeyShortcut} to respond to a specific key combination for a context.
    * 
    * @param shortcut to register
    * 
    * @return a {@link HandlerRegistration} that can be used to un-register the shortcut
    */
   public HandlerRegistration register(KeyShortcut shortcut)
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
    * Register a {@link SurplusKeyListener} to catch all non-shortcut key events for a context.
    * 
    * @param listener
    * @return a {@link HandlerRegistration} that can be used to un-register the listener
    */
   public HandlerRegistration register(SurplusKeyListener listener)
   {
      Set<SurplusKeyListener> listeners = ensureSurplusKeyListenerMap().get(listener.getContext());
      if (listeners == null)
      {
         listeners = new HashSet<SurplusKeyListener>();
         ensureSurplusKeyListenerMap().put(listener.getContext(), listeners);
      }
      listeners.add(listener);
      return new SurplusKeyListenerHandlerRegistration(listener);
   }

   /**
    * Check for active shortcuts for the entered key combination,
    * trigger events in handlers, then if no shortcuts have been
    * triggered, fire any registered {@link SurplusKeyListener}
    * events.
    * 
    * @param evt
    */
   private void processKeyEvent(NativeEvent evt)
   {
      int modifiers = calculateModifiers(evt);
      int keyHash = calculateKeyHash(modifiers, evt.getKeyCode());
      Log.debug("processing key shortcut for key" + evt.getKeyCode() + " with hash " + keyHash);
      Set<KeyShortcut> shortcuts = ensureShortcutMap().get(keyHash);
      boolean shortcutFound = false;
      KeyShortcutEvent shortcutEvent = new KeyShortcutEvent(modifiers, evt.getKeyCode());
      if (shortcuts != null && !shortcuts.isEmpty())
      {
         for (KeyShortcut shortcut : shortcuts)
         {
            if (ensureActiveContexts().contains(shortcut.getContext()) && shortcut.getKeyEvent().nativeEventType.equals(evt.getType()))
            {
               shortcutFound = true;
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

      if(!shortcutFound)
      {
         for (ShortcutContext context : ensureActiveContexts())
         {
            Set<SurplusKeyListener> listeners = ensureSurplusKeyListenerMap().get(context);
            if (listeners != null && !listeners.isEmpty())
            {
               for (SurplusKeyListener listener : listeners)
               {
                  if (listener.getKeyEvent().nativeEventType.equals(evt.getType()))
                  {
                     // could add interface with these methods to reduce duplication
                     if (listener.isStopPropagation())
                     {
                        evt.stopPropagation();
                     }
                     if (listener.isPreventDefault())
                     {
                        evt.preventDefault();
                     }
                     listener.getHandler().onKeyShortcut(shortcutEvent);
                  }
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
   private Map<ShortcutContext, Set<SurplusKeyListener>> ensureSurplusKeyListenerMap()
   {
      if (surplusKeyMap == null)
      {
         surplusKeyMap = new HashMap<ShortcutContext, Set<SurplusKeyListener>>();
      }
      return surplusKeyMap;
   }

   private class SurplusKeyListenerHandlerRegistration implements HandlerRegistration
   {

      private SurplusKeyListener listener;

      public SurplusKeyListenerHandlerRegistration(SurplusKeyListener listener)
      {
         this.listener = listener;
      }

      @Override
      public void removeHandler()
      {
         Set<SurplusKeyListener> listeners = ensureSurplusKeyListenerMap().get(listener.getContext());
         if (listeners != null)
         {
            listeners.remove(listener);
         }
      }

   }
}
