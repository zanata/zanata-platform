package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;

public class EnableModalNavigationEvent extends GwtEvent<EnableModalNavigationEventHandler>
{
   /**
    * Handler type.
    */
   private static Type<EnableModalNavigationEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<EnableModalNavigationEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<EnableModalNavigationEventHandler>();
      }
      return TYPE;
   }

   private boolean isEnable;

   public EnableModalNavigationEvent(boolean isEnable)
   {
      this.isEnable = isEnable;
   }

   @Override
   protected void dispatch(EnableModalNavigationEventHandler handler)
   {
      handler.onEnable(this);
   }

   @Override
   public Type<EnableModalNavigationEventHandler> getAssociatedType()
   {
      return getType();
   }

   public boolean isEnable()
   {
      return isEnable;
   }

   public void setEnable(boolean isEnable)
   {
      this.isEnable = isEnable;
   }


}
