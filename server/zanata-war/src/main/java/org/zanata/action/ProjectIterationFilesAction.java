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

import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HDocument;
import org.zanata.process.IterationZipFileBuildProcess;
import org.zanata.process.IterationZipFileBuildProcessHandle;
import org.zanata.process.ProcessHandle;
import org.zanata.security.BaseSecurityChecker;

@Name("projectIterationFilesAction")
@Scope(ScopeType.CONVERSATION)
public class ProjectIterationFilesAction extends BaseSecurityChecker
{

   private String projectSlug;
   
   private String iterationSlug;

   private String localeId;
   
   @In
   private DocumentDAO documentDAO;
   
   @In
   private ProjectIterationDAO projectIterationDAO;
   
   @In
   private IterationZipFileBuildProcess iterationZipFileBuildProcess; 
   
   private List<HDocument> iterationDocuments;
   
   private String documentNameFilter;
   
   private ProcessHandle zipFilePrepHandle;
   
   
   public void initialize()
   {
      this.iterationDocuments = this.documentDAO.getAllByProjectIteration(this.projectSlug, this.iterationSlug);
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
   
   @Begin(join = true)
   @Restrict("#{projectIterationFilesAction.checkPermission('download-all')}")
   public void prepareIterationZipFile()
   {
      if( this.zipFilePrepHandle != null && this.zipFilePrepHandle.isInProgress() )
      {
         // Cancel any other processes
         this.zipFilePrepHandle.setShouldStop(true);
      }
      
      // Build a background process Handle
      IterationZipFileBuildProcessHandle processHandle =
            new IterationZipFileBuildProcessHandle();
      processHandle.setProjectSlug( this.projectSlug );
      processHandle.setIterationSlug( this.iterationSlug );
      processHandle.setLocaleId( this.localeId );
      processHandle.setInitiatingUserName( Identity.instance().getCredentials().getUsername() );
      
      // Fire the zip file building process
      this.iterationZipFileBuildProcess.startProcess( processHandle );
      this.zipFilePrepHandle = processHandle;
   }
   
   @End
   public void cancelFileDownload()
   {
      if( this.zipFilePrepHandle.isInProgress() )
      {
         this.zipFilePrepHandle.setShouldStop(true);
      }
   }
   
   @Override
   public Object getSecuredEntity()
   {
      return this.projectIterationDAO.getBySlug(this.projectSlug, this.iterationSlug);
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

   public ProcessHandle getZipFilePrepHandle()
   {
      return zipFilePrepHandle;
   }

   public void setZipFilePrepHandle(ProcessHandle zipFilePrepProgress)
   {
      this.zipFilePrepHandle = zipFilePrepProgress;
   }
   
}
