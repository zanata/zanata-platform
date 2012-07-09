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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.Keys;
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
import com.google.gwt.view.client.ListDataProvider;
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
      void addContext(String contextName, ListDataProvider<KeyShortcut> shortcuts);

      void showPanel();

      public void clearPanel();

      boolean isShowing();

      void hide(boolean autoClosed);
   }

   /**
    * Key uses {@link Keys#hashCode()}
    */
   private Map<Keys, Set<KeyShortcut>> shortcutMap;

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

      register(new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ESCAPE), ShortcutContext.Application,
            messages.closeShortcutView(), KeyEvent.KEY_UP, true, true, new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            if (display.isShowing())
            {
               display.hide(true);
            }
         }
      }));

      // could try to use ?, although this is not as simple as passing character
      // '?'
      register(new KeyShortcut(new Keys(Keys.ALT_KEY, 'Y'), ShortcutContext.Application,
            messages.showAvailableKeyShortcuts(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            display.clearPanel();
            for (ShortcutContext context : ensureActiveContexts())
            {
               ListDataProvider<KeyShortcut> dataProvider = new ListDataProvider<KeyShortcut>();

               for (Set<KeyShortcut> shortcutSet : ensureShortcutMap().values())
               {
                  for (KeyShortcut shortcut : shortcutSet)
                  {
                     if (shortcut.getContext() == context && shortcut.isDisplayInView() && !dataProvider.getList().contains(shortcut))
                     {
                        dataProvider.getList().add(shortcut);
                     }
                  }
               }
               Collections.sort(dataProvider.getList());
               display.addContext(getContextName(context), dataProvider);
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
      Log.debug("Registering key shortcut. Key codes follow:");
      for (Keys keys : shortcut.getAllKeys())
      {
         Log.debug("    " + keys.toString());
         Set<KeyShortcut> shortcuts = ensureShortcutMap().get(keys);
         if (shortcuts == null)
         {
            shortcuts = new HashSet<KeyShortcut>();
            ensureShortcutMap().put(keys, shortcuts);
         }
         shortcuts.add(shortcut);
      }
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
      Keys pressedKeys = new Keys(evt);
      Set<KeyShortcut> shortcuts = ensureShortcutMap().get(pressedKeys);
      boolean shortcutFound = false;
      // TODO replace modifiers + keycode in event with Keys
      KeyShortcutEvent shortcutEvent = new KeyShortcutEvent(pressedKeys.getModifiers(), pressedKeys.getKeyCode());
      if (shortcuts != null && !shortcuts.isEmpty())
      {
         for (KeyShortcut shortcut : shortcuts)
         {
            boolean contextActive = ensureActiveContexts().contains(shortcut.getContext());
            boolean matchingEventType = shortcut.getKeyEvent().nativeEventType.equals(evt.getType());
            if (contextActive && matchingEventType)
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

   private String getContextName(ShortcutContext context)
   {
      String contextName = "";
      switch (context)
      {
      case Application:
         contextName = messages.applicationScope();
         break;
      case ProjectWideSearch:
         contextName = messages.projectWideSearchAndReplace();
         break;
      case Edit:
         contextName = messages.editScope();
         break;
      case Navigation:
         contextName = messages.navigationScope();
         break;
      }
      return contextName;
   }

   private Set<ShortcutContext> ensureActiveContexts()
   {
      if (activeContexts == null)
      {
         activeContexts = new HashSet<ShortcutContext>();
      }
      return activeContexts;
   }

   private Map<Keys, Set<KeyShortcut>> ensureShortcutMap()
   {
      if (shortcutMap == null)
      {
         shortcutMap = new HashMap<Keys, Set<KeyShortcut>>();
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
         for (Keys keys : shortcut.getAllKeys())
         {
            Set<KeyShortcut> shortcuts = ensureShortcutMap().get(keys);
            if (shortcuts != null)
            {
               shortcuts.remove(shortcut);
            }
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
