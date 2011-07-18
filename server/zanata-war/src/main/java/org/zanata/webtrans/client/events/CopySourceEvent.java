package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.shared.GwtEvent;

public class CopySourceEvent extends GwtEvent<CopySourceEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<CopySourceEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<CopySourceEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<CopySourceEventHandler>();
      }
      return TYPE;
   }

   private TransUnit value;

   public CopySourceEvent(TransUnit rowValue)
   {
      this.value = rowValue;
   }

   public TransUnit getTransUnit()
   {
      return value;
   }

   @Override
   protected void dispatch(CopySourceEventHandler handler)
   {
      handler.onCopySource(this);
   }

   @Override
   public Type<CopySourceEventHandler> getAssociatedType()
   {
      return getType();
   }

}