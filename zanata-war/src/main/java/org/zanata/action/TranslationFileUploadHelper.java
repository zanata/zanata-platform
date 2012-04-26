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

import java.io.InputStream;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class TranslationFileUploadHelper
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
