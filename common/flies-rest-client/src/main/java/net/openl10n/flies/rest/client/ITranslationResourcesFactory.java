package net.openl10n.flies.rest.client;

import java.net.URI;

import net.openl10n.flies.rest.client.ITranslationResources;

public interface ITranslationResourcesFactory
{
   ITranslationResources getTranslationResources(String projectSlug, String versionSlug);

   URI getTranslationResourcesURI(String projectSlug, String versionSlug);
}
