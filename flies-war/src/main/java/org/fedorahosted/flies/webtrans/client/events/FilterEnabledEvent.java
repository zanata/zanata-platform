package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.webtrans.client.filter.ContentFilter;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;

import com.google.gwt.event.shared.GwtEvent;

public class FilterEnabledEvent extends GwtEvent<FilterEnabledEventHandler>
{

   private final ContentFilter<TransUnit> contentFilter;

   public FilterEnabledEvent(ContentFilter<TransUnit> contentFilter)
   {
      this.contentFilter = contentFilter;
   }

   /**
    * Handler type.
    */
   private static Type<FilterEnabledEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<FilterEnabledEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<FilterEnabledEventHandler>();
      }
      return TYPE;
   }

   @Override
   protected void dispatch(FilterEnabledEventHandler handler)
   {
      handler.onFilterEnabled(this);
   }

   @Override
   public Type<FilterEnabledEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   public ContentFilter<TransUnit> getContentFilter()
   {
      return contentFilter;
   }

}
