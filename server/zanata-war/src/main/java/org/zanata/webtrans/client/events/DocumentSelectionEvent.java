package org.zanata.webtrans.client.events;

import org.zanata.webtrans.shared.model.DocumentId;
import com.google.gwt.event.shared.GwtEvent;

public class DocumentSelectionEvent extends GwtEvent<DocumentSelectionHandler>
{

   /**
    * Handler type.
    */
   private static Type<DocumentSelectionHandler> TYPE;
   private final DocumentId document;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<DocumentSelectionHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<DocumentSelectionHandler>());
   }

   public DocumentSelectionEvent(DocumentId documentId)
   {
      this.document = documentId;
   }

   public DocumentId getDocumentId()
   {
      return document;
   }

   @Override
   protected void dispatch(DocumentSelectionHandler handler)
   {
      handler.onDocumentSelected(this);
   }

   @Override
   public GwtEvent.Type<DocumentSelectionHandler> getAssociatedType()
   {
      return getType();
   }

}
