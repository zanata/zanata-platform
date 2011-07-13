package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.shared.GwtEvent;

public class ToggleFuzzyEvent extends GwtEvent<ToggleFuzzyEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<ToggleFuzzyEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<ToggleFuzzyEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<ToggleFuzzyEventHandler>();
      }
      return TYPE;
   }

   private TransUnit value;

   public ToggleFuzzyEvent(TransUnit rowValue)
   {
      this.value = rowValue;
   }

   public TransUnit getTransUnit()
   {
      return value;
   }

   @Override
   protected void dispatch(ToggleFuzzyEventHandler handler)
   {
      handler.onToggleFuzzy(this);
   }

   @Override
   public Type<ToggleFuzzyEventHandler> getAssociatedType()
   {
      return getType();
   }

}