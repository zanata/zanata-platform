package org.zanata.webtrans.shared.model;

import org.zanata.common.TranslationStats;

import com.google.gwt.user.client.rpc.IsSerializable;


public class DocumentStatus implements IsSerializable
{
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
