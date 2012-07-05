package org.zanata.webtrans.client.keys;

import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;

/**
 * Represents a catcher for key events that do not trigger key shortcuts, for
 * registration with {@link KeyShortcutPresenter}
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public class SurplusKeyListener
{

   private final KeyEvent keyEvent;
   private final ShortcutContext context;
   private final boolean stopPropagation;
   private final boolean preventDefault;
   private final KeyShortcutEventHandler handler;

   /**
    * Create a surplus key listener, optionally preventing propagation or
    * default actions of native events.
    * 
    * @param keyEvent which key event to respond to
    * @param context part of the application in which this listener should be active
    * @param stopPropagation
    * @param preventDefault
    * @param handler
    */
   public SurplusKeyListener(KeyEvent keyEvent, ShortcutContext context, boolean stopPropagation, boolean preventDefault, KeyShortcutEventHandler handler)
   {
      this.keyEvent = keyEvent;
      this.context = context;
      this.stopPropagation = stopPropagation;
      this.preventDefault = preventDefault;
      this.handler = handler;
   }

   /**
    * Create a surplus key listener that does not prevent propagation or default actions of native
    * events.
    * 
    * @param keyEvent
    * @param context
    * @param handler
    * 
    * @see #SurplusKeyListener(KeyEvent, ShortcutContext, boolean, boolean, KeyShortcutEventHandler)
    */
   public SurplusKeyListener(KeyEvent keyEvent, ShortcutContext context, KeyShortcutEventHandler handler)
   {
      this(keyEvent, context, false, false, handler);
   }

   public KeyEvent getKeyEvent()
   {
      return keyEvent;
   }

   public ShortcutContext getContext()
   {
      return context;
   }

   public boolean isStopPropagation()
   {
      return stopPropagation;
   }

   public boolean isPreventDefault()
   {
      return preventDefault;
   }

   public KeyShortcutEventHandler getHandler()
   {
      return handler;
   }

   @Override
   public int hashCode()
   {
      return keyEvent.ordinal() + context.ordinal() * 8;
   }

   /**
    * Two {@link SurplusKeyListener} objects are equal if they have the same key
    * event type and context.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof SurplusKeyListener))
         return false;
      SurplusKeyListener other = (SurplusKeyListener) obj;
      return keyEvent == other.keyEvent && context == other.context;
   }
}
