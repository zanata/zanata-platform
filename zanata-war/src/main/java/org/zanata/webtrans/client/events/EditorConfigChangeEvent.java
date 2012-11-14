package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class EditorConfigChangeEvent extends GwtEvent<EditorConfigChangeHandler>
{
   public static final EditorConfigChangeEvent EVENT = new EditorConfigChangeEvent();
   /**
    * Handler type.
    */
   private static Type<EditorConfigChangeHandler> TYPE;

   private EditorConfigChangeEvent()
   {
   }

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<EditorConfigChangeHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<EditorConfigChangeHandler>();
      }
      return TYPE;
   }

   @Override
   protected void dispatch(EditorConfigChangeHandler handler)
   {
      handler.onUserConfigChanged(this);
   }

   @Override
   public Type<EditorConfigChangeHandler> getAssociatedType()
   {
      return getType();
   }

}