package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class BookmarkableTextFlowEvent extends GwtEvent<BookmarkableTextFlowEventHandler>
{
   public static Type<BookmarkableTextFlowEventHandler> TYPE = new Type<BookmarkableTextFlowEventHandler>();

   private TransUnitId textFlowId;

   public BookmarkableTextFlowEvent(TransUnitId textFlowId)
   {
      this.textFlowId = textFlowId;
   }

   public Type<BookmarkableTextFlowEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(BookmarkableTextFlowEventHandler handler)
   {
      handler.onBookmarkableTextFlow(this);
   }

   public TransUnitId getTextFlowId()
   {
      return textFlowId;
   }
}
