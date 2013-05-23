package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

//@Immutable
public class DocumentStatus implements IsSerializable, Serializable
{
   private static final long serialVersionUID = 1L;

   private final DocumentId documentid;
   private final boolean hasError;
   private final Date lastTranslatedDate;
   private final String lastTranslatedBy;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentStatus()
   {
      this(null, false, null, null);
   }

   public DocumentStatus(DocumentId documentid, boolean hasError, Date lastTranslatedDate, String lastTranslatedBy)
   {
      this.documentid = documentid;
      this.hasError = hasError;
      this.lastTranslatedDate = lastTranslatedDate;
      this.lastTranslatedBy = lastTranslatedBy;
   }

   public DocumentId getDocumentid()
   {
      return documentid;
   }

   public boolean hasError()
   {
      return hasError;
   }

   public Date getLastTranslatedDate()
   {
      return lastTranslatedDate;
   }

   public String getLastTranslatedBy()
   {
      return lastTranslatedBy;
   }

}
