package net.openl10n.flies.webtrans.client.events;

import net.openl10n.flies.webtrans.shared.model.DocumentInfo;

import com.google.gwt.event.shared.GwtEvent;

public class DocumentSelectionEvent extends GwtEvent<DocumentSelectionHandler>
{

   /**
    * Handler type.
    */
   private static Type<DocumentSelectionHandler> TYPE;
   private final DocumentInfo document;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<DocumentSelectionHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<DocumentSelectionHandler>());
   }

   public DocumentSelectionEvent(DocumentInfo document)
   {
      this.document = document;
   }

   public DocumentInfo getDocument()
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
