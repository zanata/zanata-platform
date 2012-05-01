package org.zanata.rest.client;

import java.net.URI;


public interface ITranslationResourcesFactory
{
   ITranslatedDocResource getTranslatedDocResource(String projectSlug, String versionSlug);

   URI getResourceURI(String projectSlug, String versionSlug);
}
