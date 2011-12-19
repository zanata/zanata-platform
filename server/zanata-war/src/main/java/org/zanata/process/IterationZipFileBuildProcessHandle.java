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
package org.zanata.process;

/**
 * Extension of the basci Process Handle class to include data pertinent to the 
 * building of an iteration zip file.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class IterationZipFileBuildProcessHandle extends ProcessHandle
{

   private String projectSlug;
   private String iterationSlug;
   private String localeId; 
   private String initiatingUserName;
   private String downloadId;
   
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
   
   public String getDownloadId()
   {
      return downloadId;
   }
   
   public void setDownloadId(String downloadId)
   {
      this.downloadId = downloadId;
   }

   public String getInitiatingUserName()
   {
      return initiatingUserName;
   }

   public void setInitiatingUserName(String initiatingUserName)
   {
      this.initiatingUserName = initiatingUserName;
   }
   
}
