package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class RefreshPageEvent extends GwtEvent<RefreshPageEventHandler>
{
   public static Type<RefreshPageEventHandler> TYPE = new Type<RefreshPageEventHandler>();
   public static final RefreshPageEvent EVENT = new RefreshPageEvent();

   private RefreshPageEvent()
   {
   }

   public Type<RefreshPageEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(RefreshPageEventHandler handler)
   {
      handler.onRefreshPage(this);
   }
}
