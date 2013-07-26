package org.zanata.file;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class GlobalDocumentId
{
   @NonNull private final String projectSlug;
   @NonNull private final String versionSlug;
   @NonNull private final String docId;

   @Override
   public String toString()
   {
      return projectSlug + ":" + versionSlug + ":" + docId;
   }
}
