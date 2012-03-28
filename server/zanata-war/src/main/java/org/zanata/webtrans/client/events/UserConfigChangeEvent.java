package org.zanata.webtrans.client.events;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.shared.GwtEvent;

public class UserConfigChangeEvent extends GwtEvent<UserConfigChangeHandler>
{

   /**
    * Handler type.
    */
   private static Type<UserConfigChangeHandler> TYPE;

   public UserConfigChangeEvent()
   {
   }

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<UserConfigChangeHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<UserConfigChangeHandler>();
      }
      return TYPE;
   }

   @Override
   protected void dispatch(UserConfigChangeHandler handler)
   {
      handler.onValueChanged(this);
   }

   @Override
   public Type<UserConfigChangeHandler> getAssociatedType()
   {
      return getType();
   }

}