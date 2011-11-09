package org.zanata.action;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.process.IterationZipFileBuildProcess;
import org.zanata.process.IterationZipFileBuildProcessHandle;
import org.zanata.process.ProcessHandle;
import org.zanata.security.BaseSecurityChecker;

@Name("projectIterationZipFileAction")
@Scope(ScopeType.CONVERSATION)
public class ProjectIterationZipFileAction extends BaseSecurityChecker
{
   
   @In
   private IterationZipFileBuildProcess iterationZipFileBuildProcess;
   
   @In
   private ProjectIterationDAO projectIterationDAO;
   
   private String projectSlug;
   
   private String iterationSlug;
   
   private String localeId;
   
   private ProcessHandle zipFilePrepHandle;

   @Begin(join = true)
   @Restrict("#{projectIterationZipFileAction.checkPermission('download-all')}")
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

   public ProcessHandle getZipFilePrepHandle()
   {
      return zipFilePrepHandle;
   }

   public void setZipFilePrepHandle(ProcessHandle zipFilePrepHandle)
   {
      this.zipFilePrepHandle = zipFilePrepHandle;
   }
}
