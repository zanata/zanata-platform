package org.zanata.file;

import lombok.Getter;

@Getter
public class GlobalDocumentId
{

   private final String projectSlug;
   private final String versionSlug;
   private final String docId;

   public GlobalDocumentId(String projectSlug, String versionSlug, String docId)
   {
      this.projectSlug = projectSlug;
      this.versionSlug = versionSlug;
      this.docId = docId;
   }

   @Override
   public String toString()
   {
      return projectSlug + ":" + versionSlug + ":" + docId;
   }
}
