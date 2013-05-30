package org.zanata.webtrans.shared.model;

import java.util.Map;

import org.zanata.common.LocaleId;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;

import com.google.gwt.user.client.rpc.IsSerializable;

public class DocumentInfo implements HasIdentifier<DocumentId>, IsSerializable
{
   private DocumentId id;
   private String name;
   private String path;
   private LocaleId sourceLocale;
   private ContainerTranslationStatistics stats;
   private AuditInfo lastModified;
   private AuditInfo lastTranslated;
   private Map<String, String> downloadExtensions;
   
   
   private Boolean hasError = null;

   // for GWT
   @SuppressWarnings("unused")
   private DocumentInfo()
   {
   }

   public DocumentInfo(DocumentId id, String name, String path, LocaleId sourceLocale, ContainerTranslationStatistics stats, AuditInfo lastModified, Map<String, String> downloadExtensions, AuditInfo lastTranslated)
   {
      this.id = id;
      this.name = name;
      this.path = path;
      this.stats = stats;
      this.sourceLocale = sourceLocale;
      this.lastModified = lastModified;
      this.downloadExtensions = downloadExtensions;
      this.lastTranslated = lastTranslated;
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

   public ContainerTranslationStatistics getStats()
   {
      return stats;
   }

   public void setStats(ContainerTranslationStatistics stats)
   {
      this.stats = stats;
   }

   public LocaleId getSourceLocale()
   {
      return sourceLocale;
   }

   public AuditInfo getLastModified()
   {
      return lastModified;
   }

   public Map<String, String> getDownloadExtensions()
   {
      return downloadExtensions;
   }

   public AuditInfo getLastTranslated()
   {
      return lastTranslated;
   }

   public void setLastTranslated(AuditInfo lastTranslated)
   {
      this.lastTranslated = lastTranslated;
   }

   public void setHasError(Boolean hasError)
   {
      this.hasError = hasError;
   }

   public Boolean hasError()
   {
      return hasError;
   }

   @Override
   public String toString()
   {
      return "DocumentInfo(name=" + name + ",path=" + path + ",id=" + id + ")";
   }

   @Override
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof DocumentInfo))
      {
         return false;
      }
      DocumentInfo other = (DocumentInfo) obj;
      return (id.equals(other.getId()));
   }

   @Override
   public int hashCode()
   {
      return id.hashCode();
   }
}
