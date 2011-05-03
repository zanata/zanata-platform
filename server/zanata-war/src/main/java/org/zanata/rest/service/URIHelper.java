package org.zanata.rest.service;

import org.zanata.rest.RestUtil;

public final class URIHelper
{

   private URIHelper()
   {
   }

   public static String getProject(String projectSlug)
   {
      return "projects/p/" + projectSlug;
   }

   public static String getIteration(String projectSlug, String iterationSlug)
   {
      return getProject(projectSlug) + "/iterations/i/" + iterationSlug;
   }

   public static String getDocument(String projectSlug, String iterationSlug, String documentId)
   {
      return getIteration(projectSlug, iterationSlug) + "/documents/d/" + RestUtil.convertToDocumentURIId(documentId);
   }

   public static String convertFromDocumentURIId(String uriId)
   {
      return uriId.replace(',', '/');
   }

   /**
    * @deprecated Use {@link RestUtil#convertToDocumentURIId(String)} instead
    */
   public static String convertToDocumentURIId(String id)
   {
      return RestUtil.convertToDocumentURIId(id);
   }

}
