package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.gwt.event.shared.GwtEvent;

public class RequestSelectTableRowEvent extends GwtEvent<RequestSelectTableRowEventHandler>
{
   public static Type<RequestSelectTableRowEventHandler> TYPE = new Type<RequestSelectTableRowEventHandler>();

   private TransUnitId selectedId;
   private boolean suppressSavePending = false;

   public RequestSelectTableRowEvent(TransUnitId transUnitId)
   {
      this.selectedId = transUnitId;
   }

   public Type<RequestSelectTableRowEventHandler> getAssociatedType()
   {
      return TYPE;
   }

   protected void dispatch(RequestSelectTableRowEventHandler handler)
   {
      handler.onRequestSelectTableRow(this);
   }

   public TransUnitId getSelectedId()
   {
      return selectedId;
   }

   public RequestSelectTableRowEvent setSuppressSavePending(boolean suppressSavePending)
   {
      this.suppressSavePending = suppressSavePending;
      return this;
   }

   public boolean isSuppressSavePending()
   {
      return suppressSavePending;
   }
}
