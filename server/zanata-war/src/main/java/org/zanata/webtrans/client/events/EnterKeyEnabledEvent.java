package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class EnterKeyEnabledEvent extends GwtEvent<EnterKeyEnabledEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<EnterKeyEnabledEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<EnterKeyEnabledEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<EnterKeyEnabledEventHandler>();
      }
      return TYPE;
   }

   private Boolean enterkeyEnabled;

   public EnterKeyEnabledEvent(Boolean value)
   {
      enterkeyEnabled = value;
   }

   public Boolean isEnabled()
   {
      return enterkeyEnabled;
   }

   @Override
   protected void dispatch(EnterKeyEnabledEventHandler handler)
   {
      handler.onValueChanged(this);
   }

   @Override
   public Type<EnterKeyEnabledEventHandler> getAssociatedType()
   {
      return getType();
   }

}