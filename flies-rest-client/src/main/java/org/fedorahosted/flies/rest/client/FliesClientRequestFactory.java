package org.fedorahosted.flies.rest.client;

import java.net.URI;

import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class FliesClientRequestFactory extends ClientRequestFactory
{
   static
   {
      ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(instance);
   }

   public FliesClientRequestFactory(String username, String apiKey)
   {
      super();
      getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
   }

   public FliesClientRequestFactory(String username, String apiKey, ClientExecutor executor)
   {
      super(executor, null, null);
      getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
   }

   public IDocumentsResource getDocumentsResource(final URI uri)
   {
      return createProxy(IDocumentsResource.class, uri);
   }

   public IProjectIterationResource getProjectIterationResource(final URI uri)
   {
      return createProxy(IProjectIterationResource.class, uri);
   }
   
   public IAccountResource getAccountResource(final URI uri) 
   {
      return createProxy(IAccountResource.class, uri);
   }

   public IProjectResource getProjectResource(final URI uri)
   {
      return createProxy(IProjectResource.class, uri);
   }

   public IProjectsResource getProjectsResource(final URI uri)
   {
      return createProxy(IProjectsResource.class, uri);
   }

   public ITranslationResources getTranslationResourcesResource(final URI uri)
   {
      return createProxy(ITranslationResources.class, uri);
   }
}

