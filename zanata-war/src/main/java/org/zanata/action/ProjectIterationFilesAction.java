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

import static org.zanata.rest.dto.stats.TranslationStatistics.StatUnit.WORD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.faces.context.FacesContext;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.Hibernate;
import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.jboss.seam.util.Hex;
import org.zanata.annotation.CachedMethods;
import org.zanata.common.DocumentType;
import org.zanata.common.EntityStatus;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.LocaleDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HRawDocument;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ProjectType;
import org.zanata.rest.dto.extensions.ExtensionType;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.rest.service.StatisticsResource;
import org.zanata.security.SecurityFunctions;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationFileService;
import org.zanata.service.TranslationService;
import org.zanata.util.StringUtil;
import org.zanata.util.ZanataMessages;

import com.google.common.base.Function;

@Name("projectIterationFilesAction")
@Scope(ScopeType.PAGE)
@CachedMethods
public class ProjectIterationFilesAction implements Serializable
{

   private static final long serialVersionUID = 1L;

   private String projectSlug;

   private String iterationSlug;

   private String localeId;

   @Logger
   private Log log;

   @In
   private ZanataIdentity identity;

   @In(required = false, value = JpaIdentityStore.AUTHENTICATED_USER)
   private HAccount authenticatedAccount;

   @In
   private DocumentDAO documentDAO;

   @In
   private LocaleDAO localeDAO;

   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   private PersonDAO personDAO;

   @In
   private TranslationFileService translationFileServiceImpl;

   @In
   private TranslationService translationServiceImpl;

   @In
   private DocumentService documentServiceImpl;

   @In
   private StatisticsResource statisticsServiceImpl;

   @In
   private ZanataMessages zanataMessages;

   private List<HDocument> iterationDocuments;

   private String documentNameFilter;

   private TranslationFileUploadHelper translationFileUpload;

   private DocumentFileUploadHelper documentFileUpload;

   private HProjectIteration projectIteration;

   private StatUnit statsOption = WORD;

   private Map<String, TranslationStatistics> statisticMap;

   public void initialize()
   {
      this.iterationDocuments = this.documentDAO.getAllByProjectIteration(this.projectSlug, this.iterationSlug);
      this.translationFileUpload = new TranslationFileUploadHelper();
      this.documentFileUpload = new DocumentFileUploadHelper();
      this.statisticMap = new HashMap<String, TranslationStatistics>();
   }

   public HLocale getLocale()
   {
      return localeDAO.findByLocaleId(new LocaleId(localeId));
   }

   public boolean filterDocumentByName(Object docObject)
   {
      final HDocument document = (HDocument) docObject;

      if (this.documentNameFilter != null && this.documentNameFilter.length() > 0)
      {
         return document.getName().toLowerCase().contains(this.documentNameFilter.toLowerCase());
      }
      else
      {
         return true;
      }
   }

   public TranslationStatistics getTransUnitForDocument(HDocument doc)
   {
      if (!statisticMap.containsKey(doc.getDocId()))
      {
         ContainerTranslationStatistics docStatistics = statisticsServiceImpl.getStatistics(this.projectSlug, this.iterationSlug, doc.getDocId(), true, new String[] { this.localeId });
         TranslationStatistics stats = docStatistics.getStats(this.localeId, statsOption);
         statisticMap.put(doc.getDocId(), stats);
         return stats;
      }
      else
      {
         return statisticMap.get(doc.getDocId());
      }
   }

   @Restrict("#{projectIterationFilesAction.documentRemovalAllowed}")
   public void deleteDocument( HDocument doc )
   {
      doc = documentDAO.getById( doc.getId() ); // refresh the instance
      documentServiceImpl.makeObsolete(doc);
   }

   @Restrict("#{projectIterationFilesAction.fileUploadAllowed}")
   public String uploadTranslationFile()
   {
      try
      {
         // process the file
         TranslationsResource transRes = translationFileServiceImpl.parseTranslationFile(translationFileUpload.getFileContents(), translationFileUpload.getFileName(), localeId);

         // translate it
         Set<String> extensions;
         if (translationFileUpload.getFileName().endsWith(".po"))
         {
            extensions = new StringSet(ExtensionType.GetText.toString());
         }
         else
         {
            extensions = Collections.<String> emptySet();
         }
         List<String> warnings = translationServiceImpl.translateAllInDoc(projectSlug, iterationSlug, translationFileUpload.getDocId(), new LocaleId(localeId), transRes, extensions, translationFileUpload.getMergeTranslations() ? MergeType.AUTO : MergeType.IMPORT);

         StringBuilder facesInfoMssg = new StringBuilder("File {0} uploaded.");
         if (!warnings.isEmpty())
         {
            facesInfoMssg.append(" There were some warnings, see below.");
         }

         FacesMessages.instance().add(Severity.INFO, facesInfoMssg.toString(), translationFileUpload.getFileName());
         for (String warning : warnings)
         {
            FacesMessages.instance().add(Severity.WARN, warning);
         }
      }
      catch (ZanataServiceException e)
      {
         FacesMessages.instance().add(Severity.ERROR, e.getMessage(), translationFileUpload.getFileName());
      }

      // NB This needs to be done as for some reason seam is losing the
      // parameters when redirecting
      // This is efectively the same as returning void
      return FacesContext.getCurrentInstance().getViewRoot().getViewId();
   }

   @Restrict("#{projectIterationFilesAction.documentUploadAllowed}")
   public String uploadDocumentFile()
   {
      if (this.documentFileUpload.getFileName().endsWith(".pot"))
      {
         uploadPotFile();
      }
      else if (translationFileServiceImpl.hasAdapterFor(documentFileUpload.getFileName()))
      {
         uploadAdapterFile();
      }
      else
      {
         FacesMessages.instance().add(Severity.ERROR, "Unrecognized file extension for {0}.", documentFileUpload.getFileName());
      }

      // NB This needs to be done as for some reason seam is losing the
      // parameters when redirecting
      // This is effectively the same as returning void
      return FacesContext.getCurrentInstance().getViewRoot().getViewId();
   }

   private void showUploadSuccessMessage()
   {
      FacesMessages.instance().add(Severity.INFO, "Document file {0} uploaded.", documentFileUpload.getFileName());
   }

   private void uploadPotFile()
   {
      try
      {
         Resource doc;
         if (documentFileUpload.getDocId() == null)
         {
            doc = translationFileServiceImpl.parseDocumentFile(documentFileUpload.getFileContents(), documentFileUpload.getDocumentPath(), documentFileUpload.getFileName());
         }
         else
         {
            doc = translationFileServiceImpl.parseUpdatedDocumentFile(documentFileUpload.getFileContents(), documentFileUpload.getDocId(), documentFileUpload.getFileName());
         }

         doc.setLang(new LocaleId(documentFileUpload.getSourceLang()));

         // TODO Copy Trans values
         documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, new StringSet(ExtensionType.GetText.toString()), false);

         showUploadSuccessMessage();
      }
      catch (ZanataServiceException e)
      {
         FacesMessages.instance().add(Severity.ERROR, e.getMessage(), documentFileUpload.getFileName());
      }
      catch (InvalidStateException e)
      {
         FacesMessages.instance().add(Severity.ERROR, "Invalid arguments");
      }
   }

   // TODO add logging for disk writing errors
   private void uploadAdapterFile()
   {
      String fileName = documentFileUpload.getFileName();
      String docId = documentFileUpload.getDocId();
      String documentPath = "";
      if (docId == null)
      {
         documentPath = documentFileUpload.getDocumentPath();
      }
      else if (docId.contains("/"))
      {
         documentPath = docId.substring(0, docId.lastIndexOf('/'));
      }

      File tempFile = null;
      byte[] md5hash;
      try
      {
         MessageDigest md = MessageDigest.getInstance("MD5");
         InputStream fileContents = new DigestInputStream(documentFileUpload.getFileContents(), md);
         tempFile = translationFileServiceImpl.persistToTempFile(fileContents);
         md5hash = md.digest();
      }
      catch (ZanataServiceException e)
      {
         log.error("Failed writing temp file for document {0}", e, documentFileUpload.getDocId());
         FacesMessages.instance().add(Severity.ERROR, "Error saving uploaded document {0} to server.", fileName);
         return;
      }
      catch (NoSuchAlgorithmException e)
      {
         log.error("MD5 hash algorithm not available", e);
         FacesMessages.instance().add(Severity.ERROR, "Error generating hash for uploaded document {0}.", fileName);
         return;
      }

      HDocument document = null;
      try
      {
         Resource doc;
         if (docId == null)
         {
            doc = translationFileServiceImpl.parseDocumentFile(tempFile.toURI(), documentPath, fileName);
         }
         else
         {
            doc = translationFileServiceImpl.parseUpdatedDocumentFile(tempFile.toURI(), docId, fileName);
         }
         doc.setLang(new LocaleId(documentFileUpload.getSourceLang()));
         Set<String> extensions = Collections.<String> emptySet();
         // TODO Copy Trans values
         document = documentServiceImpl.saveDocument(projectSlug, iterationSlug, doc, extensions, false);
         showUploadSuccessMessage();
      }
      catch (SecurityException e)
      {
         FacesMessages.instance().add(Severity.ERROR, "Error reading uploaded document {0} on server.", fileName);
      }
      catch (ZanataServiceException e)
      {
         FacesMessages.instance().add(Severity.ERROR, "Invalid document format for {0}.", fileName);
      }

      if (document == null)
      {
         // error message for failed parse already added.
      }
      else
      {
         HRawDocument rawDocument = new HRawDocument();
         rawDocument.setDocument(document);
         rawDocument.setContentHash(new String(Hex.encodeHex(md5hash)));
         rawDocument.setType(DocumentType.typeFor(translationFileServiceImpl.extractExtension(fileName)));
         rawDocument.setUploadedBy(identity.getCredentials().getUsername());

         FileInputStream tempFileStream = null;
         try
         {
            tempFileStream = new FileInputStream(tempFile);
            Blob fileContents = Hibernate.createBlob(tempFileStream, (int) tempFile.length());
            rawDocument.setContent(fileContents);
            documentDAO.addRawDocument(document, rawDocument);
            documentDAO.flush();
         }
         catch (FileNotFoundException e)
         {
            log.error("Failed to open stream from temp source file", e);
            FacesMessages.instance().add(Severity.ERROR, "Error saving uploaded document {0} on server, download in original format may fail.", documentFileUpload.getFileName());
         }
      }

      translationFileServiceImpl.removeTempFile(tempFile);
   }

   public List<HLocale> getAvailableSourceLocales()
   {
      return localeDAO.findAllActive();
   }

   public boolean isFileUploadAllowed()
   {
      return isIterationActive() && identity != null && identity.hasPermission("modify-translation", getProjectIteration().getProject(), getLocale());
   }

   public boolean isDocumentUploadAllowed()
   {
      HProjectIteration projectIteration = this.projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      return isIterationActive() && identity != null && identity.hasPermission("import-template", projectIteration);
   }

   public boolean isDocumentRemovalAllowed()
   {
      // currently same permissions as uploading a document
      return this.isDocumentUploadAllowed();
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

   // line not found
   public boolean hasOriginal(String docPath, String docName)
   {
      return translationFileServiceImpl.hasPersistedDocument(projectSlug, iterationSlug, docPath, docName);
   }

   public String extensionOf(String docPath, String docName)
   {
      return "." + translationFileServiceImpl.getFileExtension(projectSlug, iterationSlug, docPath, docName);
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

   public HProjectIteration getProjectIteration()
   {
      if (this.projectIteration == null)
      {
         this.projectIteration = projectIterationDAO.getBySlug(projectSlug, iterationSlug);
      }
      return this.projectIteration;
   }

   public StatUnit getStatsOption()
   {
      return statsOption;
   }

   public void setStatsOption(StatUnit statsOption)
   {
      this.statsOption = statsOption;
   }

   public boolean isUserAllowedToTranslate()
   {
      return isIterationActive() && identity != null && identity.hasPermission("add-translation", getProjectIteration().getProject(), getLocale());
   }

   public boolean isIterationReadOnly()
   {
      return getProjectIteration().getProject().getStatus() == EntityStatus.READONLY || getProjectIteration().getStatus() == EntityStatus.READONLY;
   }

   public boolean isIterationObsolete()
   {
      return getProjectIteration().getProject().getStatus() == EntityStatus.OBSOLETE || getProjectIteration().getStatus() == EntityStatus.OBSOLETE;
   }
   
   public boolean isIterationActive()
   {
      return getProjectIteration().getProject().getStatus() == EntityStatus.ACTIVE || getProjectIteration().getStatus() == EntityStatus.ACTIVE;
   }

   public boolean isZipFileDownloadAllowed()
   {
      return getProjectIteration().getProjectType() == ProjectType.Gettext ||
             getProjectIteration().getProjectType() == ProjectType.Podir;
   }

   public String getZipFileDownloadTitle()
   {
      String message = null;
      if( !isZipFileDownloadAllowed() )
      {
         if( getProjectIteration().getProjectType() == null )
         {
            message = zanataMessages.getMessage("jsf.iteration.files.DownloadAllFiles.ProjectTypeNotSet");
         }
         else if( getProjectIteration().getProjectType() != ProjectType.Gettext &&
                  getProjectIteration().getProjectType() != ProjectType.Podir )
         {
            message = zanataMessages.getMessage("jsf.iteration.files.DownloadAllFiles.ProjectTypeNotAllowed");
         }
      }
      else
      {
         message = zanataMessages.getMessage("jsf.iteration.files.DownloadAllFiles.title");
      }
      return message;
   }

   /**
    * Returns the display zanataMessages to show when a user cannot translate.
    */
   public List<String> getTranslationDeniedReasonMessages()
   {
      List<String> displayMessages = new ArrayList<String>(5);
      final String separator = "<br/>";

      // Account not logged in
      if (identity == null)
      {
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.NotLoggedIn"));
         return displayMessages;
      }

      // Iteration is read only
      if (isIterationReadOnly())
      {
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.VersionIsReadOnly"));
      }

      // Iteration is Obsolete
      if (isIterationObsolete())
      {
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.VersionIsObsolete"));
      }

      // User not member of language team
      if (!personDAO.isMemberOfLanguageTeam(authenticatedAccount.getPerson(), getLocale()))
      {
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.UserNotInLanguageTeam", getLocale().retrieveDisplayName()));
      }

      // User not part of the allowed roles
      HProject project = getProjectIteration().getProject();
      if (!SecurityFunctions.isUserAllowedAccess(project))
      {
         // jsf.iteration.files.translateDenied.UserNotInProjectRole
         displayMessages.add(zanataMessages.getMessage("jsf.iteration.files.translateDenied.UserNotInProjectRole", StringUtil.concat(project.getAllowedRoles(), ',', new Function<HAccountRole, String>()
         {
            @Override
            public String apply(@Nullable
            HAccountRole from)
            {
               return from.getName();
            }
         })));
      }

      return displayMessages;
   }

   /**
    * Helper class to upload translation files.
    */
   public static class TranslationFileUploadHelper implements Serializable
   {
      private static final long serialVersionUID = 1L;

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
   public static class DocumentFileUploadHelper implements Serializable
   {
      private static final long serialVersionUID = 1L;

      @Getter
      @Setter
      private InputStream fileContents;

      @Getter
      @Setter
      private String docId;

      @Getter
      @Setter
      private String fileName;

      // TODO rename to customDocumentPath (update in EL also)
      @Getter
      @Setter
      private String documentPath;

      @Getter
      @Setter
      private String sourceLang = "en-US"; // en-US by default
   }
}
