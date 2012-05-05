package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import org.zanata.common.TranslationStats;


public class DocumentStatus implements Serializable
{
   private static final long serialVersionUID = 1L;

   private TranslationStats count;
   private DocumentId documentid;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentStatus()
   {
   }

   public DocumentStatus(DocumentId id, TranslationStats count)
   {
      this.documentid = id;
      this.count = count;
   }

   public DocumentId getDocumentid()
   {
      return documentid;
   }

   public TranslationStats getCount()
   {
      return count;
   }
}
