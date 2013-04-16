package org.zanata.webtrans.shared.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocumentStatus implements IsSerializable, Serializable
{
   private static final long serialVersionUID = 1L;

   private DocumentId documentid;
   private boolean hasError;
   private Date lastTranslatedDate;
   private String lastTranslatedBy;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentStatus()
   {
   }

   public DocumentStatus(DocumentId documentid, boolean hasError, Date lastTranslatedDate, String lastTranslatedBy)
   {
      this.documentid = documentid;
      update(hasError, lastTranslatedDate, lastTranslatedBy);
   }

   public void update(boolean hasError, Date lastTranslatedDate, String lastTranslatedBy)
   {
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

   public void setHasError(boolean hasError)
   {
      this.hasError = hasError;
   }

   public void setLastTranslatedDate(Date lastTranslatedDate)
   {
      this.lastTranslatedDate = lastTranslatedDate;
   }

   public void setLastTranslatedBy(String lastTranslatedBy)
   {
      this.lastTranslatedBy = lastTranslatedBy;
   }

}
