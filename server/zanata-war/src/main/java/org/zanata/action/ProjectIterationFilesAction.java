/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import static org.jboss.seam.international.StatusMessage.Severity;

@Name("projectIterationFilesAction")
@Scope(ScopeType.PAGE)
public class ProjectIterationFilesAction
{

   private String projectSlug;
   
   private String iterationSlug;

   private String localeId;

   @In
   private ZanataIdentity identity;

   @In
   private DocumentDAO documentDAO;
   
   @In
   private LocaleDAO localeDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private TranslationFileService translationFileServiceImpl;

   @In
   private TranslationService translationServiceImpl;

   @In
   private DocumentService documentServiceImpl;

   private List<HDocument> iterationDocuments;
   
   private String documentNameFilter;

   private TranslationFileUploadHelper translationFileUpload;

   private DocumentFileUploadHelper documentFileUpload;
   
   
   public void initialize()
   {
      this.iterationDocuments = this.documentDAO.getAllByProjectIteration(this.projectSlug, this.iterationSlug);
      this.translationFileUpload = new TranslationFileUploadHelper();
      this.documentFileUpload = new DocumentFileUploadHelper();
   }
   
   public HLocale getLocale()
   {
      return localeDAO.findByLocaleId(new LocaleId(localeId));
   }

   public boolean filterDocumentByName( Object docObject )
   {
      final HDocument document = (HDocument)docObject;
      
      if( this.documentNameFilter != null && this.documentNameFilter.length() > 0 )
      {
         return document.getName().toLowerCase().contains( this.documentNameFilter.toLowerCase() );
      }
      else
      {
         return true;
      }
   }
   
   public TransUnitWords getTransUnitWordsForDocument(HDocument doc)
   {
      TranslationStats documentStats = this.documentDAO.getStatistics(doc.getId(), new LocaleId(this.localeId));
      return documentStats.getWordCount();
   }

   @Restrict("#{projectIterationFilesAction.fileUploadAllowed}")
   public void uploadTranslationFile()
   {
      TranslationsResource transRes = null;
      try
      {
         // process the file
         transRes = this.translationFileServiceImpl.parseTranslationFile(this.translationFileUpload.getFileContents(),
               this.translationFileUpload.getFileName());

         // translate it
         Collection<TextFlowTarget> resourcesNotFound =
            this.translationServiceImpl.translateAll(this.projectSlug, this.iterationSlug, this.translationFileUpload.getDocId(),
               new LocaleId(this.localeId), transRes, new StringSet(ExtensionType.GetText.toString()),
               this.translationFileUpload.getMergeTranslations() ? MergeType.AUTO : MergeType.IMPORT);

         StringBuilder facesInfoMssg = new StringBuilder("File {0} uploaded.");
         if( resourcesNotFound.size() > 0 )
         {
            facesInfoMssg.append(" There were some warnings, see below.");
         }

         FacesMessages.instance().add(Severity.INFO, facesInfoMssg.toString(), this.translationFileUpload.getFileName());
         for( TextFlowTarget nf : resourcesNotFound )
         {
            FacesMessages.instance().add(Severity.WARN, "Could not find text flow for message: {0}", nf.getContents());
         }
      }
      catch (ZanataServiceException zex)
      {
         FacesMessages.instance().add(Severity.ERROR, zex.getMessage(), this.translationFileUpload.getFileName());
      }
   }

   @Restrict("#{projectIterationFilesAction.documentUploadAllowed}")
   public void uploadDocumentFile()
   {
      try
      {
         Resource doc = this.translationFileServiceImpl.parseDocumentFile(this.documentFileUpload.getFileContents(),
              this.documentFileUpload.getDocumentPath(), this.documentFileUpload.getFileName());

         // TODO Copy Trans values
         // Extensions are hard-coded to GetText, since it is the only supported format at the time
         this.documentServiceImpl.saveDocument(this.projectSlug, this.iterationSlug,
               this.documentFileUpload.getDocumentPath() + doc.getName(), doc, new StringSet(ExtensionType.GetText.toString()),
               false);

         FacesMessages.instance().add(Severity.INFO, "Document file {0} uploaded.", this.documentFileUpload.getFileName());
      }
      catch (ZanataServiceException zex)
      {
         FacesMessages.instance().add(Severity.ERROR, zex.getMessage(), this.documentFileUpload.getFileName());
      }
   }

   public boolean isFileUploadAllowed()
   {
      HProjectIteration projectIteration = this.projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      HLocale hLocale = this.localeDAO.findByLocaleId( new LocaleId(localeId) );

      return projectIteration.getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("modify-translation", projectIteration, hLocale);
   }

   public boolean isDocumentUploadAllowed()
   {
      HProjectIteration projectIteration = this.projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      return projectIteration.getStatus() == EntityStatus.ACTIVE
            && identity != null && identity.hasPermission("import-template", projectIteration);
   }

   public List<HDocument> getIterationDocuments()
   {
      return iterationDocuments;
   }

   public void setIterationDocuments(List<HDocument> iterationDocuments)
   {
      this.iterationDocuments = iterationDocuments;
   }

   public String getProjectSlug()
   {
      return projectSlug;
   }

   public void setProjectSlug(String projectSlug)
   {
      this.projectSlug = projectSlug;
   }

   public String getIterationSlug()
   {
      return iterationSlug;
   }

   public void setIterationSlug(String iterationSlug)
   {
      this.iterationSlug = iterationSlug;
   }

   public String getLocaleId()
   {
      return localeId;
   }

   public void setLocaleId(String localeId)
   {
      this.localeId = localeId;
   }

   public String getDocumentNameFilter()
   {
      return documentNameFilter;
   }

   public void setDocumentNameFilter(String documentNameFilter)
   {
      this.documentNameFilter = documentNameFilter;
   }

   public TranslationFileUploadHelper getTranslationFileUpload()
   {
      return translationFileUpload;
   }

   public DocumentFileUploadHelper getDocumentFileUpload()
   {
      return documentFileUpload;
   }

   /**
    * Helper class to upload translation files.
    */
   public static class TranslationFileUploadHelper
   {
      private String docId;

      private InputStream fileContents;

      private String fileName;

      private boolean mergeTranslations = true; // Merge by default


      public String getDocId()
      {
         return docId;
      }

      public void setDocId(String docId)
      {
         this.docId = docId;
      }

      public InputStream getFileContents()
      {
         return fileContents;
      }

      public void setFileContents(InputStream fileContents)
      {
         this.fileContents = fileContents;
      }

      public String getFileName()
      {
         return fileName;
      }

      public void setFileName(String fileName)
      {
         this.fileName = fileName;
      }

      public boolean getMergeTranslations()
      {
         return mergeTranslations;
      }

      public void setMergeTranslations(boolean mergeTranslations)
      {
         this.mergeTranslations = mergeTranslations;
      }
   }

   /**
    * Helper class to upload documents.
    */
   public static class DocumentFileUploadHelper
   {
      private InputStream fileContents;

      private String fileName;

      private String documentPath;

      public InputStream getFileContents()
      {
         return fileContents;
      }

      public void setFileContents(InputStream fileContents)
      {
         this.fileContents = fileContents;
      }

      public String getFileName()
      {
         return fileName;
      }

      public void setFileName(String fileName)
      {
         this.fileName = fileName;
      }

      public String getDocumentPath()
      {
         return documentPath;
      }

      public void setDocumentPath(String documentPath)
      {
         this.documentPath = documentPath;
      }
   }
}
