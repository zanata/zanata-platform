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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

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
      void addContext(String title, List<String> shortcuts);

      void showPanel();

      public void clearPanel();

      //hide method not provided as auto-hide is enabled
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

            if ((event.getTypeInt() & Event.ONKEYDOWN) != 0)
            {
               Log.debug("Event type: " + evt.getType());

               processKeyEvent(evt);

               // Log.debug("Key event. Stopping propagation.");
               // evt.stopPropagation();
               // evt.preventDefault();
            }
         }
      });

      // could try to use ?, although this is not as simple as passing character '?'
      registerKeyShortcut(new KeyShortcut(KeyShortcut.ALT_KEY, 'Y',
            ShortcutContext.Application,
            messages.showAvailableKeyShortcuts(),
            new KeyShortcutEventHandler()
      {

         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            display.clearPanel();
            for (ShortcutContext context : ensureActiveContexts())
            {
               ArrayList<String> shortcutStrings = new ArrayList<String>();
               for (Set<KeyShortcut> shortcutSet : ensureShortcutMap().values())
               {
                  for (KeyShortcut shortcut : shortcutSet)
                  {
                     if (shortcut.getContext() == context)
                     {
                        StringBuilder sb = new StringBuilder();
                        if ((shortcut.getModifiers() & KeyShortcut.CTRL_KEY) != 0)
                        {
                           sb.append("Ctrl+");
                        }
                        if ((shortcut.getModifiers() & KeyShortcut.SHIFT_KEY) != 0)
                        {
                           sb.append("Shift+");
                        }
                        if ((shortcut.getModifiers() & KeyShortcut.META_KEY) != 0)
                        {
                           sb.append("Meta+");
                        }
                        if ((shortcut.getModifiers() & KeyShortcut.ALT_KEY) != 0)
                        {
                           sb.append("Alt+");
                        }
                        sb.append((char) shortcut.getKeyCode());
                        sb.append(" : ");
                        sb.append(shortcut.getDescription());
                        shortcutStrings.add(sb.toString());
                     }
                  }
               }

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
               display.addContext(contextName, shortcutStrings);
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
            // TODO throw exception? Remove this check? Just warn but still remove context?
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
      Log.info("registering key shortcut. key: " + shortcut.getKeyCode() + " modifier: " + shortcut.getModifiers() + " keyhash: " + shortcut.keysHash());
      Set<KeyShortcut> shortcuts = ensureShortcutMap().get(shortcut.keysHash());
      if (shortcuts == null)
      {
         shortcuts = new HashSet<KeyShortcut>();
         ensureShortcutMap().put(shortcut.keysHash(), shortcuts);
      }
      shortcuts.add(shortcut);
      return new KeyShortcutHandlerRegistration(shortcut);
   }

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
            if (ensureActiveContexts().contains(shortcut.getContext()))
            {
               shortcut.getHandler().onKeyShortcut(shortcutEvent);
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
