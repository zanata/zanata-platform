package org.fedorahosted.flies.webtrans.client.events;

import org.fedorahosted.flies.webtrans.shared.model.DocumentId;
import org.fedorahosted.flies.webtrans.shared.model.TransUnitId;
import org.fedorahosted.flies.webtrans.shared.rpc.HasTransUnitEditData;

import com.google.gwt.event.shared.GwtEvent;

public class TransUnitEditEvent extends GwtEvent<TransUnitEditEventHandler> implements HasTransUnitEditData
{

   private final TransUnitId transUnitId;
   private final DocumentId documentId;
   private final String sessionId;

   /**
    * Handler type.
    */
   private static Type<TransUnitEditEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<TransUnitEditEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<TransUnitEditEventHandler>();
      }
      return TYPE;
   }

   public TransUnitEditEvent(HasTransUnitEditData data)
   {
      this.documentId = data.getDocumentId();
      this.transUnitId = data.getTransUnitId();
      this.sessionId = data.getSessionId();
   }

   @Override
   protected void dispatch(TransUnitEditEventHandler handler)
   {
      handler.onTransUnitEdit(this);
   }

   @Override
   public Type<TransUnitEditEventHandler> getAssociatedType()
   {
      return getType();
   }

   @Override
   public DocumentId getDocumentId()
   {
      return documentId;
   }

   @Override
   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   @Override
   public String getSessionId()
   {
      return sessionId;
   }

}
