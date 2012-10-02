package org.zanata.rest.files;

import java.io.InputStream;
import java.io.Serializable;

import javax.ws.rs.FormParam;

import lombok.Getter;
import lombok.Setter;

public class DocumentFileUploadForm implements Serializable
{
   private static final long serialVersionUID = 1L;

   // FIXME this could break if two clients try to upload the same docId at the same time
   // would end up with a chimera file with parts from each, it would process when one of them
   // is finished, then there would be an error when the other sends the next part (as temp file
   // would be removed after processing).

   @FormParam("file")
   @Getter
   @Setter
   private InputStream fileStream;

   @FormParam("type")
   @Getter
   @Setter
   private String fileType;

   @FormParam("uploadId")
   @Getter
   @Setter
   private Long uploadId;

   @FormParam("first")
   @Getter
   @Setter
   private Boolean first;

   @FormParam("last")
   @Getter
   @Setter
   private Boolean last;

   @Getter
   @Setter
   @FormParam("hash")
   private String hash;
}