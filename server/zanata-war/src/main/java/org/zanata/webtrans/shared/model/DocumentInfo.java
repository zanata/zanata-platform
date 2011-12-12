package org.zanata.webtrans.shared.model;

import java.io.Serializable;

import org.zanata.common.TranslationStats;

public class DocumentInfo implements HasIdentifier<DocumentId>, Serializable
{
   private static final long serialVersionUID = 1L;
   private DocumentId id;
   private String name;
   private String path;
   private TranslationStats stats;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentInfo()
   {
   }

   public DocumentInfo(DocumentId id, String name, String path, TranslationStats stats)
   {
      this.id = id;
      this.name = name;
      this.path = path;
      this.stats = stats;
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

   /**
    * @return the stats
    */
   public TranslationStats getStats()
   {
      return stats;
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

}
