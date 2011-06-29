package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class TextChangeEvent extends GwtEvent<TextChangeEventHandler>
{
   /**
    * Handler type.
    */
   public static Type<TextChangeEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<TextChangeEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<TextChangeEventHandler>();
      }
      return TYPE;
   }

   private final String message;

   public TextChangeEvent(String message)
   {
      this.message = message;
   }

   @Override
   protected void dispatch(TextChangeEventHandler handler)
   {
      handler.onTextChange(this);
   }

   @Override
   public GwtEvent.Type<TextChangeEventHandler> getAssociatedType()
   {
      return getType();
   }

}
