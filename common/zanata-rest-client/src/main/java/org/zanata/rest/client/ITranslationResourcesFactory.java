package org.zanata.rest.client;

import java.net.URI;


public interface ITranslationResourcesFactory
{
   ITranslationResources getTranslationResources(String projectSlug, String versionSlug);

   URI getTranslationResourcesURI(String projectSlug, String versionSlug);
}
