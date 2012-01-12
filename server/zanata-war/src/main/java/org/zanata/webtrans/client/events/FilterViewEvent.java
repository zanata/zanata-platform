package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class FilterViewEvent extends GwtEvent<FilterViewEventHandler>
{
   /**
    * Handler type.
    */
   private static Type<FilterViewEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<FilterViewEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<FilterViewEventHandler>();
      }
      return TYPE;
   }

   private boolean filterTranslated, filterNeedReview, filterUntranslated;

   public FilterViewEvent(boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated)
   {
      this.filterTranslated = filterTranslated;
      this.filterNeedReview = filterNeedReview;
      this.filterUntranslated = filterUntranslated;
   }

   @Override
   protected void dispatch(FilterViewEventHandler handler)
   {
      handler.onFilterView(this);
   }

   @Override
   public Type<FilterViewEventHandler> getAssociatedType()
   {
      return getType();
   }

   public boolean isFilterTranslated()
   {
      return filterTranslated;
   }

   public boolean isFilterNeedReview()
   {
      return filterNeedReview;
   }

   public boolean isFilterUntranslated()
   {
      return filterUntranslated;
   }

}