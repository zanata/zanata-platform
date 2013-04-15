package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class AliasKeyChangedEvent extends GwtEvent<AliasKeyChangedEventHandler>
{
   /**
    * Handler type.
    */
   private static Type<AliasKeyChangedEventHandler> TYPE = new Type<AliasKeyChangedEventHandler>();

    /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<AliasKeyChangedEventHandler> getType()
   {
      return TYPE;
   }

   private final boolean isAliasKeyListening;

   public AliasKeyChangedEvent(boolean isAliasKeyListening)
   {
      this.isAliasKeyListening = isAliasKeyListening;
   }

   public boolean isAliasKeyListening()
   {
      return isAliasKeyListening;
   }

   @Override
   protected void dispatch(AliasKeyChangedEventHandler handler)
   {
      handler.onAliasKeyChanged(this);
   }

   @Override
   public Type<AliasKeyChangedEventHandler> getAssociatedType()
   {
      return getType();
   }

}