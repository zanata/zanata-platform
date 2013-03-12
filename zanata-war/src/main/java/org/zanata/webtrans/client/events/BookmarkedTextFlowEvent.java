package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class BookmarkedTextFlowEvent extends GwtEvent<BookmarkedTextFlowEventHandler>
{
   public static Type<BookmarkedTextFlowEventHandler> TYPE = new Type<BookmarkedTextFlowEventHandler>();

   private TransUnitId textFlowId;

   public BookmarkedTextFlowEvent(TransUnitId textFlowId)
   {
      this.textFlowId = textFlowId;
   }

   public Type<BookmarkedTextFlowEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(BookmarkedTextFlowEventHandler handler)
   {
      handler.onBookmarkableTextFlow(this);
   }

   public TransUnitId getTextFlowId()
   {
      return textFlowId;
   }
}
