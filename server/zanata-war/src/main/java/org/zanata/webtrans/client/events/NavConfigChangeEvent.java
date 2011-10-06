package org.zanata.webtrans.client.events;

import java.util.HashMap;
import java.util.Map;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TableConstants;

import com.google.gwt.event.shared.GwtEvent;

public class NavConfigChangeEvent extends GwtEvent<NavConfigChangeHandler>
{

   /**
    * Handler type.
    */
   private static Type<NavConfigChangeHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<NavConfigChangeHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<NavConfigChangeHandler>();
      }
      return TYPE;
   }

   private HashMap<ContentState, Boolean> configMap;

   public NavConfigChangeEvent(Map<ContentState, Boolean> configMap)
   {
      this.configMap = (HashMap<ContentState, Boolean>) configMap;
   }

   public Map<ContentState, Boolean> getConfigMap()
   {
      return configMap;
   }

   @Override
   protected void dispatch(NavConfigChangeHandler handler)
   {
      handler.onValueChanged(this);
   }

   @Override
   public Type<NavConfigChangeHandler> getAssociatedType()
   {
      return getType();
   }

}