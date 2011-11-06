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
package org.zanata.service;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public interface FileSystemService
{
   public enum DownloadDescriptorProperties
   {
      DownloadFileName,
      PhysicalFileName,
      OriginatingUserId;
   }
   
   /**
    * Creates a descriptor file for a download. 
    * 
    * @param physicalFile The physical file in the file system that will be downloaded.
    * @param downloadFileName The file name that should be used when downloading the file. 
    * @param generatingUser The user name that originally generated this download file.
    * @return The download Id that uniquely identifies the download.
    */
   String createDownloadDescriptorFile(File physicalFile, String downloadFileName, String generatingUser)
   throws IOException;
   
   /**
    * Returns a writable file where a generated download can be built/staged while it's being
    * prepared.
    * 
    * @param fileExtension File extension of the generated file.
    * @throws IOException
    */
   File createDownloadStagingFile(String fileExtension) throws IOException;
   
   /**
    * Returns all download files that are expired.
    */
   File[] getAllExpiredDownloadFiles();
   
   /**
    * Finds the properties for a download.
    * 
    * @param downloadId The download identifier
    * @return The properties contained in th download descriptor file. Null, if the descriptor file does not exist.
    * @throws IOException
    */
   Properties findDownloadDescriptorProperties(String downloadId) throws IOException;
   
   /**
    * Finds an actual file to be downloaded.
    * 
    * @param downloadId The download identifier.
    * @return A file with the contents of the pshysical file to be downloaded. Null, if a descriptor file for
    * the given downloadId, or an actual staging file for the download is not found.
    * @throws IOException
    */
   File findDownloadFile(String downloadId) throws IOException;
}
