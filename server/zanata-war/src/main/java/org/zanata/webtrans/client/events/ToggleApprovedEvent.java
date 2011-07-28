package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.shared.GwtEvent;

public class ToggleApprovedEvent extends GwtEvent<ToggleApprovedEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ToggleApprovedEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ToggleApprovedEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<ToggleApprovedEventHandler>();
      }
      return TYPE;
   }

   private TransUnit value;

   public ToggleApprovedEvent(TransUnit rowValue)
   {
      this.value = rowValue;
   }

   public TransUnit getTransUnit()
   {
      return value;
   }

   @Override
   protected void dispatch(ToggleApprovedEventHandler handler)
   {
      handler.onToggleApproved(this);
   }

   @Override
   public Type<ToggleApprovedEventHandler> getAssociatedType()
   {
      return getType();
   }

}