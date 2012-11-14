package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

public class DocumentListPageSizeChangeEvent extends GwtEvent<DocumentListPageSizeChangeEventHandler>
{
   public static Type<DocumentListPageSizeChangeEventHandler> TYPE = new Type<DocumentListPageSizeChangeEventHandler>();

   private int pageSize;

   public DocumentListPageSizeChangeEvent(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public Type<DocumentListPageSizeChangeEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(DocumentListPageSizeChangeEventHandler handler)
   {
      handler.onPageSizeChange(this);
   }

   public int getPageSize()
   {
      return pageSize;
   }
}
