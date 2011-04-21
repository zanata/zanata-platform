package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class NotificationEvent extends GwtEvent<NotificationEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<NotificationEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<NotificationEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<NotificationEventHandler>();
      }
      return TYPE;
   }

   public static enum Severity
   {
      Warning, Error, Info
   };

   private final Severity severity;
   private final String message;

   public NotificationEvent(Severity severity, String message)
   {
      this.severity = severity;
      this.message = message;
   }

   public Severity getSeverity()
   {
      return severity;
   }

   public String getMessage()
   {
      return message;
   }

   @Override
   protected void dispatch(NotificationEventHandler handler)
   {
      handler.onNotification(this);
   }

   @Override
   public Type<NotificationEventHandler> getAssociatedType()
   {
      return getType();
   }

}
