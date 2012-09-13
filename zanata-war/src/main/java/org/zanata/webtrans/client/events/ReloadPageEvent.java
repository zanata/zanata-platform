package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class ReloadPageEvent extends GwtEvent<ReloadPageEventHandler>
{
   public static Type<ReloadPageEventHandler> TYPE = new Type<ReloadPageEventHandler>();
   public static final ReloadPageEvent EVENT = new ReloadPageEvent();

   private ReloadPageEvent()
   {
   }

   public Type<ReloadPageEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(ReloadPageEventHandler handler)
   {
      handler.onReloadPage(this);
   }
}
