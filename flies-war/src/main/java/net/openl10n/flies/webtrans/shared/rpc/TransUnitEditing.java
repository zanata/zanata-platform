package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnitId;

//@ExposeEntity 
public class TransUnitEditing implements SessionEventData, HasTransUnitEditData
{

   private static final long serialVersionUID = 1L;

   private TransUnitId transUnitId;
   private DocumentId documentId;
   private String sessionId;

   // for ExposeEntity
   public TransUnitEditing()
   {
   }

   public TransUnitEditing(DocumentId documentId, TransUnitId transUnitId, String sessionId)
   {
      this.documentId = documentId;
      this.transUnitId = transUnitId;
      this.sessionId = sessionId;
   }

   @Override
   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public void setDocumentId(DocumentId documentId)
   {
      this.documentId = documentId;
   }

   @Override
   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public void setTransUnitId(TransUnitId transUnitId)
   {
      this.transUnitId = transUnitId;
   }

   @Override
   public String getSessionId()
   {
      return sessionId;
   }

   public void setSessionId(String sessionId)
   {
      this.sessionId = sessionId;
   }
}