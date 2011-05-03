package org.zanata.webtrans.client.editor.filter;

import com.google.gwt.event.shared.GwtEvent;

public class FilterDisabledEvent extends GwtEvent<FilterDisabledEventHandler>
{

   /**
    * Handler type.
    */
   private static Type<FilterDisabledEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<FilterDisabledEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<FilterDisabledEventHandler>();
      }
      return TYPE;
   }

   @Override
   protected void dispatch(FilterDisabledEventHandler handler)
   {
      handler.onFilterDisabled(this);
   }

   @Override
   public Type<FilterDisabledEventHandler> getAssociatedType()
   {
      return TYPE;
   }

}
