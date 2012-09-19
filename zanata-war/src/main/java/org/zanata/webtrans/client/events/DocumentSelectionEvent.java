package org.zanata.webtrans.client.events;

import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.common.base.Preconditions;
import com.google.gwt.event.shared.GwtEvent;

public class DocumentSelectionEvent extends GwtEvent<DocumentSelectionHandler> implements NavigationService.UpdateContextCommand
{

   /**
    * Handler type.
    */
   private static Type<DocumentSelectionHandler> TYPE;
   private final DocumentId document;
   private String findMessage;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<DocumentSelectionHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<DocumentSelectionHandler>());
   }

   public DocumentSelectionEvent(DocumentId document, String findMessage)
   {
      this.document = document;
      this.findMessage = findMessage;
   }

   public DocumentSelectionEvent(DocumentId documentId)
   {
      this(documentId, null);
   }

   public DocumentId getDocumentId()
   {
      return document;
   }

   public String getFindMessage()
   {
      return findMessage;
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

   @Override
   public GetTransUnitActionContext updateContext(GetTransUnitActionContext currentContext)
   {
      Preconditions.checkNotNull(currentContext, "current context can not be null");
      return currentContext.changeDocument(document).changeFindMessage(findMessage);
   }
}
