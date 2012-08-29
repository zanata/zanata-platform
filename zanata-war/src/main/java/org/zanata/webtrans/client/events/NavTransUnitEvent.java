package org.zanata.webtrans.client.events;


import com.google.gwt.event.shared.GwtEvent;

public class NavTransUnitEvent extends GwtEvent<NavTransUnitHandler>
{
   public enum NavigationType
   {
      PrevEntry, NextEntry, PrevState, NextState, FirstEntry, LastEntry
   }

   private NavigationType rowType;

   /**
    * Handler type.
    */
   private static Type<NavTransUnitHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<NavTransUnitHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<NavTransUnitHandler>());
   }


   public NavTransUnitEvent(NavigationType typeValue)
   {
      this.rowType = typeValue;
   }


   @Override
   protected void dispatch(NavTransUnitHandler handler)
   {
      handler.onNavTransUnit(this);
   }

   @Override
   public GwtEvent.Type<NavTransUnitHandler> getAssociatedType()
   {
      return getType();
   }

   public NavigationType getRowType()
   {
      return rowType;
   }
}