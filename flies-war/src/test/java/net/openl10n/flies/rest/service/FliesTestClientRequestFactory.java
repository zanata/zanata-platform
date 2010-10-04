package net.openl10n.flies.rest.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.resteasy.client.ClientExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.IDocumentsResource;
import net.openl10n.flies.rest.client.IProjectIterationResource;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.client.IVersion;
import net.openl10n.flies.rest.dto.VersionInfo;

public class FliesTestClientRequestFactory extends FliesClientRequestFactory
{
   public FliesTestClientRequestFactory(URI base, String username, String apiKey, ClientExecutor executor, VersionInfo ver)
   {
      super(base, username, apiKey, executor, ver);
   }

   private static final Logger log = LoggerFactory.getLogger(FliesTestClientRequestFactory.class);


   public IProjectIterationResource getProjectIteration(final URI uri)
   {
      return createProxy(IProjectIterationResource.class, uri);
   }

   public ITranslationResources getTranslationResources(String projectSlug, String versionSlug)
   {
      try
      {
         log.debug("create proxy for ITranslationResources");
         return createProxy(ITranslationResources.class, new URI("/restv1/projects/p/" + projectSlug + "/iterations/i/" + versionSlug + "/r"));
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   public IDocumentsResource getDocuments(String projectSlug, String iter)
   {
      try
      {
         return createProxy(IDocumentsResource.class, new URI("/restv1/projects/p/" + projectSlug + "/iterations/i/" + iter + "/documents"));
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void registerPrefixInterceptor(Object interceptor)
   {
      super.registerPrefixInterceptor(interceptor);
   }

   public IVersion getVersionInfo()
   {
      try
      {
         return (IVersion) createProxy(IVersion.class, new URI("/restv1/version"));
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

}
