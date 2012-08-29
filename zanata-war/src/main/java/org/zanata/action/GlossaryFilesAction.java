package org.zanata.action;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.validator.InvalidStateException;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.international.StatusMessage.Severity;
import org.zanata.annotation.CachedMethods;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.Glossary;
import org.zanata.service.GlossaryFileService;

@Name("glossaryFilesAction")
@Scope(ScopeType.PAGE)
@CachedMethods
public class GlossaryFilesAction implements Serializable
{
   /**
    * 
    */
   private static final long serialVersionUID = 1L;

   @In
   private LocaleDAO localeDAO;
   
   @In
   private GlossaryFileService glossaryFileServiceImpl;

   private GlossaryFileUploadHelper glossaryFileUpload;

   public void initialize()
   {
      glossaryFileUpload = new GlossaryFileUploadHelper();
   }

   public List<HLocale> getAvailableLocales()
   {
      return localeDAO.findAllActive();
   }

   public GlossaryFileUploadHelper getGlossaryFileUpload()
   {
      return glossaryFileUpload;
   }

   public void uploadFile()
   {
      try
      {
         List<Glossary> glossaries = glossaryFileServiceImpl.parseGlossaryFile(glossaryFileUpload.getFileContents(), glossaryFileUpload.getFileName(), glossaryFileUpload.getSourceLocaleId(), glossaryFileUpload.getTransLocaleId(), glossaryFileUpload.treatSourceCommentsAsTarget, glossaryFileUpload.getCommentColsList());

         for (Glossary glossary : glossaries)
         {
            glossaryFileServiceImpl.saveGlossary(glossary);
         }

         FacesMessages.instance().add(Severity.INFO, "Glossary file {0} uploaded.", this.glossaryFileUpload.getFileName());
      }
      catch (ZanataServiceException zex)
      {
         FacesMessages.instance().add(Severity.ERROR, zex.getMessage(), this.glossaryFileUpload.getFileName());
      }
      catch (InvalidStateException isex)
      {
         FacesMessages.instance().add(Severity.ERROR, "Invalid arguments");
      }

      // NB This needs to be done as for some reason seam is losing the
      // parameters when redirecting
      // This is efectively the same as returning void
      // return FacesContext.getCurrentInstance().getViewRoot().getViewId();
   }

   /**
    * Helper class to upload glossary files.
    */
   public class GlossaryFileUploadHelper
   {
      private InputStream fileContents;
      private String fileName;
      private String sourceLang = "en-US";
      private String transLang;
      private boolean treatSourceCommentsAsTarget = false;
      private String commentCols = "pos,description";

      public InputStream getFileContents()
      {
         return fileContents;
      }

      public LocaleId getTransLocaleId()
      {
         return new LocaleId(getTransLang());
      }

      public LocaleId getSourceLocaleId()
      {
         return new LocaleId(getSourceLang());
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

      public String getSourceLang()
      {
         return sourceLang;
      }

      public void setSourceLang(String sourceLang)
      {
         this.sourceLang = sourceLang;
      }

      public String getTransLang()
      {
         return transLang;
      }

      public void setTransLang(String transLang)
      {
         this.transLang = transLang;
      }

      public boolean isTreatSourceCommentsAsTarget()
      {
         return treatSourceCommentsAsTarget;
      }

      public void setTreatSourceCommentsAsTarget(boolean treatSourceCommentsAsTarget)
      {
         this.treatSourceCommentsAsTarget = treatSourceCommentsAsTarget;
      }

      public String getCommentCols()
      {
         return commentCols;
      }

      public void setCommentCols(String commentCols)
      {
         this.commentCols = commentCols;
      }

      public List<String> getCommentColsList()
      {
         String[] commentHeadersList = StringUtils.split(getCommentCols(), ",");
         List<String> list = new ArrayList<String>();
         if (commentHeadersList != null && commentHeadersList.length > 0)
         {
            Collections.addAll(list, commentHeadersList);
         }
         return list;
      }
   }
}
