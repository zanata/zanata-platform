package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class AttentionModeActivationEvent extends GwtEvent<AttentionModeActivationEventHandler>
{
   /**
    * Handler type.
    */
   private static Type<AttentionModeActivationEventHandler> TYPE = new Type<AttentionModeActivationEventHandler>();

    /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<AttentionModeActivationEventHandler> getType()
   {
      return TYPE;
   }

   private final boolean active;

   public AttentionModeActivationEvent(boolean isActive)
   {
      this.active = isActive;
   }

   public boolean isActive()
   {
      return active;
   }

   @Override
   protected void dispatch(AttentionModeActivationEventHandler handler)
   {
      handler.onAttentionModeActivationChanged(this);
   }

   @Override
   public Type<AttentionModeActivationEventHandler> getAssociatedType()
   {
      return getType();
   }

}