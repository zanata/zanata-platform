package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationController;
import com.google.common.base.Objects;
import com.google.gwt.event.shared.GwtEvent;

public class FilterViewEvent extends GwtEvent<FilterViewEventHandler> implements NavigationController.UpdateContextCommand
{
   /**
    * Handler type.
    */
   private static Type<FilterViewEventHandler> TYPE;
   public static final FilterViewEvent DEFAULT = new FilterViewEvent(false, false, false, false);

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
   private boolean cancelFilter;

   public FilterViewEvent(boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated, boolean cancelFilter)
   {
      this.filterTranslated = filterTranslated;
      this.filterNeedReview = filterNeedReview;
      this.filterUntranslated = filterUntranslated;
      this.cancelFilter = cancelFilter;
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

   public boolean isCancelFilter()
   {
      return cancelFilter;
   }

   @Override
   public GetTransUnitActionContext updateContext(GetTransUnitActionContext currentContext)
   {
      return currentContext.setFilterNeedReview(filterNeedReview).setFilterTranslated(filterTranslated).setFilterUntranslated(filterUntranslated);
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).
            add("filterTranslated", filterTranslated).
            add("filterNeedReview", filterNeedReview).
            add("filterUntranslated", filterUntranslated).
            add("cancelFilter", cancelFilter).
            toString();
   }
}