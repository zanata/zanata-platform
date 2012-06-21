package org.zanata.webtrans.client.events;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.InlineHyperlink;

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
   }

   private final Severity severity;
   private final String message;
   private String linkText;
   private ClickHandler linkClickHandler;

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

   /**
    * append a link to message
    * @param linkText link text
    * @param clickHandler action to perform when click on the link
    * @return notification event itself
    */
   public NotificationEvent appendInlineLinkToMessage(String linkText, ClickHandler clickHandler)
   {
      this.linkText = linkText;
      this.linkClickHandler = clickHandler;
      return this;
   }

   public String getLinkText()
   {
      return linkText;
   }

   public ClickHandler getLinkClickHandler()
   {
      return linkClickHandler;
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
