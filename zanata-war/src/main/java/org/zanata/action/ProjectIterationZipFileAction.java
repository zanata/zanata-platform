package org.zanata.action;

import java.io.Serializable;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.security.Identity;
import org.zanata.ApplicationConfiguration;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.process.IterationZipFileBuildProcess;
import org.zanata.process.IterationZipFileBuildProcessHandle;
import org.zanata.process.ProcessHandle;
import org.zanata.service.ProcessManagerService;

@Name("projectIterationZipFileAction")
@Scope(ScopeType.CONVERSATION)
public class ProjectIterationZipFileAction implements Serializable
{
   
   private static final long serialVersionUID = 1L;
   
   @In
   private ProjectIterationDAO projectIterationDAO;

   @In
   ProcessManagerService processManagerServiceImpl;

   @In
   private ApplicationConfiguration applicationConfiguration;
   
   private String projectSlug;
   
   private String iterationSlug;
   
   private String localeId;
   
   private ProcessHandle zipFilePrepHandle;

   @Begin(join = true)
   @Restrict("#{s:hasPermission(projectIterationZipFileAction.projectIteration, 'download-all')}")
   public void prepareIterationZipFile()
   {
      if( this.zipFilePrepHandle != null && this.zipFilePrepHandle.isInProgress() )
      {
         // Cancel any other processes
         this.zipFilePrepHandle.stop();
      }
      
      // Build a background process Handle
      IterationZipFileBuildProcessHandle processHandle =
            new IterationZipFileBuildProcessHandle();
      this.zipFilePrepHandle = processHandle;      
      processHandle.setProjectSlug( this.projectSlug );
      processHandle.setIterationSlug( this.iterationSlug );
      processHandle.setLocaleId( this.localeId );
      processHandle.setInitiatingUserName( Identity.instance().getCredentials().getUsername() );
      processHandle.setServerPath(applicationConfiguration.getServerPath()); // This needs to be done here as the server
                                                                             // path may not be available when running
                                                                             // asynchronously
      
      // Fire the zip file building process and wait until it is ready to return
      this.processManagerServiceImpl.startProcess( new IterationZipFileBuildProcess(), processHandle );
      processHandle.waitUntilReady();
   }
   
   @End
   public void cancelFileDownload()
   {
      if( this.zipFilePrepHandle.isInProgress() )
      {
         this.zipFilePrepHandle.stop();
      }
   }
   
   public Object getProjectIteration()
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
