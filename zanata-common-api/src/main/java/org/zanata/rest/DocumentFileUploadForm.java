/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest;

import java.io.InputStream;
import java.io.Serializable;
import javax.ws.rs.FormParam;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

/**
 * Form for use in file uploads.
 * 
 * For upload of a document as a single chunk, 'first' and 'last' should both be
 * set to TRUE (uploadId is ignored). For chunked upload, send the first part
 * with first=TRUE, then send the returned uploadId from the first part with
 * subsequent parts. The document is parsed when the final part is sent with
 * last=TRUE.
 * 
 * Hash should always be set to the md5 hash of the entire document. For chunked
 * upload, the hash is checked against the complete document after the chunks
 * are joined.
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 * @see MultipartForm
 */
public class DocumentFileUploadForm implements Serializable
{
   private static final long serialVersionUID = 1L;

   @FormParam("file")
   @PartType("application/octet-stream")
   private InputStream fileStream;

   @FormParam("type")
   @PartType("text/plain")
   private String fileType;

   @FormParam("uploadId")
   @PartType("text/plain")
   private Long uploadId;

   @FormParam("first")
   @PartType("text/plain")
   private Boolean first;

   @FormParam("last")
   @PartType("text/plain")
   private Boolean last;

   @FormParam("hash")
   @PartType("text/plain")
   private String hash;

   public InputStream getFileStream()
   {
      return fileStream;
   }

   public void setFileStream(InputStream fileStream)
   {
      this.fileStream = fileStream;
   }

   public String getFileType()
   {
      return fileType;
   }

   public void setFileType(String fileType)
   {
      this.fileType = fileType;
   }

   public Long getUploadId()
   {
      return uploadId;
   }

   public void setUploadId(Long uploadId)
   {
      this.uploadId = uploadId;
   }

   public Boolean getFirst()
   {
      return first;
   }

   public void setFirst(Boolean first)
   {
      this.first = first;
   }

   public Boolean getLast()
   {
      return last;
   }

   public void setLast(Boolean last)
   {
      this.last = last;
   }

   public String getHash()
   {
      return hash;
   }

   public void setHash(String hash)
   {
      this.hash = hash;
   }
}
