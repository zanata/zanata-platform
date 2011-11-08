package org.zanata.rest.client;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.resteasy.client.ClientExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.client.IProjectIterationResource;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.client.IVersion;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.VersionInfo;


public class TestProxyFactory extends ZanataProxyFactory
{
   private static final Logger log = LoggerFactory.getLogger(TestProxyFactory.class);
   private static final String AUTH_KEY = "b6d7044e9ee3b2447c28fb7c50d86d98";
   private static final String USERNAME = "admin";

   public TestProxyFactory(URI base, String username, String apiKey, ClientExecutor executor, VersionInfo ver)
   {
      super(base, username, apiKey, executor, ver);
   }

   public TestProxyFactory(ClientExecutor executor) throws URISyntaxException
   {
      this(new URI("http://example.com/"), USERNAME, AUTH_KEY, executor, new VersionInfo("SNAPSHOT", ""));
   }

   public IProjectIterationResource getProjectIteration(final URI uri)
   {
      return createProxy(IProjectIterationResource.class, uri);
   }

   @Override
   public ITranslationResources getTranslationResources(String projectSlug, String versionSlug)
   {
      try
      {
         log.debug("create proxy for ITranslationResources");
         return createProxy(ITranslationResources.class, new URI("/restv1/projects/p/" + projectSlug + "/iterations/i/" + versionSlug + "/r"));
      }
      catch (URISyntaxException e)
      {
         log.debug("exception:" + e.getMessage());
         throw new RuntimeException(e);
      }
   }

   @Override
   public void registerPrefixInterceptor(Object interceptor)
   {
      super.registerPrefixInterceptor(interceptor);
   }

   @Override
   public IVersion createIVersion()
   {
      try
      {
         return createProxy(IVersion.class, new URI("/restv1/version"));
      }
      catch (URISyntaxException e)
      {
         log.debug("exception:" + e.getMessage());
         throw new RuntimeException(e);
      }
   }

}
