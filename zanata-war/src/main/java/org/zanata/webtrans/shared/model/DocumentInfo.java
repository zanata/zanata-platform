package org.zanata.webtrans.shared.model;

import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocumentInfo implements HasIdentifier<DocumentId>, IsSerializable
{
   private DocumentId id;
   private String name;
   private String path;
   private LocaleId sourceLocale;
   private TranslationStats stats;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentInfo()
   {
   }

   public DocumentInfo(DocumentId id, String name, String path, LocaleId sourceLocale, TranslationStats stats)
   {
      this.id = id;
      this.name = name;
      this.path = path;
      this.stats = stats;
      this.sourceLocale = sourceLocale;
   }

   public DocumentId getId()
   {
      return id;
   }

   public String getName()
   {
      return name;
   }

   public String getPath()
   {
      return path;
   }

   public TranslationStats getStats()
   {
      return stats;
   }

   public LocaleId getSourceLocale()
   {
      return sourceLocale;
   }

   @Override
   public String toString()
   {
      return "DocumentInfo(name=" + name + ",path=" + path + ",id=" + id + ")";
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof DocumentInfo))
         return false;
      DocumentInfo other = (DocumentInfo) obj;
      return (id.equals(other.getId()));
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }
}
