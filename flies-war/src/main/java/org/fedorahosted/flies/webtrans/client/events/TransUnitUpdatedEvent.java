package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.common.ContentState;
import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;
import org.fedorahosted.flies.webtrans.shared.rpc.HasTransUnitUpdatedData;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitUpdatedEvent extends GwtEvent<TransUnitUpdatedEventHandler> implements HasTransUnitUpdatedData
{

   private final TransUnitId transUnitId;
   private final DocumentId documentId;
   private final ContentState previousStatus;
   private final ContentState newStatus;

   /**
    * Handler type.
    */
   private static Type<TransUnitUpdatedEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<TransUnitUpdatedEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<TransUnitUpdatedEventHandler>();
      }
      return TYPE;
   }

   public TransUnitUpdatedEvent(HasTransUnitUpdatedData data)
   {
      this.documentId = data.getDocumentId();
      this.newStatus = data.getNewStatus();
      this.previousStatus = data.getPreviousStatus();
      this.transUnitId = data.getTransUnitId();
   }

   @Override
   protected void dispatch(TransUnitUpdatedEventHandler handler)
   {
      handler.onTransUnitUpdated(this);
   }

   @Override
   public Type<TransUnitUpdatedEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   public DocumentId getDocumentId()
   {
      return documentId;
   }

   @Override
   public ContentState getNewStatus()
   {
      return newStatus;
   };

   @Override
   public ContentState getPreviousStatus()
   {
      return previousStatus;
   }

   @Override
   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

}
