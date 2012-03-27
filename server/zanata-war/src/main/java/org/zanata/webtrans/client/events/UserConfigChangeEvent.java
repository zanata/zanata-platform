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

   private Map<String, Boolean> configMap;

   public UserConfigChangeEvent(Map<String, Boolean> configMap)
   {
      //TODO instead of passing config map around, all receiver should directly reference UserConfigHolder to get value
      this.configMap = configMap;
   }

   public Map<String, Boolean> getConfigMap()
   {
      return configMap;
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