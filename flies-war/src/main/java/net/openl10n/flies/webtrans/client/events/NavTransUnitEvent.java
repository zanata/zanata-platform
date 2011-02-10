package net.openl10n.flies.webtrans.client.events;


import com.google.gwt.event.shared.GwtEvent;

public class NavTransUnitEvent extends GwtEvent<NavTransUnitHandler>
{
   public enum NavigationType
   {
      PrevEntry, NextEntry, PrevFuzzyOrUntranslated, NextFuzzyOrUntranslated,
   }

   private static int DEFAULT_STEP = 1;
   private NavigationType rowType;
   private int step;

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



   /**
    * NavigationType may be PrevEntry, NextEntry, PrevFuzzyOrUntranslated or
    * NextFuzzyOrUntranslated.
    * 
    * @param typeValue
    * @param stepValue
    */
   public NavTransUnitEvent(NavigationType typeValue, int stepValue)
   {
      this.rowType = typeValue;
      this.step = stepValue;
   }

   public NavTransUnitEvent(NavigationType typeValue)
   {
      this.rowType = typeValue;
      this.step = DEFAULT_STEP;
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