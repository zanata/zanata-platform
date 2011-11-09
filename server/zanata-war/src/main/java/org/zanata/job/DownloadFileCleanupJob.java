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
package org.zanata.job;

import java.io.File;
import java.io.FileFilter;
import java.util.Calendar;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.zanata.service.FileSystemService;

/**
 * Scheduled job that cleans up old files remaining from the Download process.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Name("downloadFileCleanupJob")
public class DownloadFileCleanupJob extends ZanataSchedulableJob
{
   @Logger
   private Log log;
   
   @In
   private FileSystemService fileSystemServiceImpl;
   
   @Override
   public String getName()
   {
      return "Download File Cleanup";
   }
   
   @Override
   protected void execute() throws Exception
   {
      File[] toRemove = this.fileSystemServiceImpl.getAllExpiredDownloadFiles();
      
      // Remove all files that match the filter
      for( File f : toRemove )
      {
         log.debug("Removing file {0}", f.getName());
         f.delete();
      }
   }
}
