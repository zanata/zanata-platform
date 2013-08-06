package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class HideReferenceEvent extends GwtEvent<HideReferenceEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<HideReferenceEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<HideReferenceEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<HideReferenceEventHandler>();
      }
      return TYPE;
   }

   public HideReferenceEvent()
   {
   }

   @Override
   public Type<HideReferenceEventHandler> getAssociatedType()
   {
      return getType();
   }


   @Override
   protected void dispatch(HideReferenceEventHandler handler)
   {
      handler.onHideReference(this);
   }   
}