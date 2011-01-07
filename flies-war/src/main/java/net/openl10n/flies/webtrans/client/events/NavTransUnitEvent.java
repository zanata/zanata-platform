package net.openl10n.flies.webtrans.client.events;

import net.openl10n.flies.common.NavigationType;

import com.google.gwt.event.shared.GwtEvent;

public class NavTransUnitEvent extends GwtEvent<NavTransUnitHandler>
{

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

   private NavigationType rowType;
   private int step;

   /**
    * ContentState may be New, NeedApproved or null. stepValue may be -1 or +1.
    * 
    * @param typeValue
    * @param stepValue
    */
   public NavTransUnitEvent(NavigationType typeValue, int stepValue)
   {
      this.rowType = typeValue;
      this.step = stepValue;
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

   public int getStep()
   {
      return step;
   }
}