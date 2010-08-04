package org.fedorahosted.flies.rest.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FliesClientRequestFactory extends ClientRequestFactory
{
   static
   {
      ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(instance);
   }

   private static final Logger log = LoggerFactory.getLogger(FliesClientRequestFactory.class);

   public FliesClientRequestFactory(String username, String apiKey)
   {
      this(null, username, apiKey);
   }

   public FliesClientRequestFactory(URI base, String username, String apiKey)
   {
      super(fixBase(base));
      getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
   }

   public FliesClientRequestFactory(URI base, String username, String apiKey, ClientExecutor executor)
   {
      super(executor, null, fixBase(base));
      getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
   }

   private static URI fixBase(URI base)
   {
      if (base != null)
      {
         String baseString = base.toString();
         if (!baseString.endsWith("/"))
         {
            try
            {
               URI result = new URI(baseString + "/");
               log.warn("Appending '/' to Flies base URL '{}': using '{}'", baseString, result);
               return result;
            }
            catch (URISyntaxException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
      return base;
   }

   public IAccountResource getAccount(final URI uri)
   {
      return createProxy(IAccountResource.class, uri);
   }

   public IDocumentsResource getDocuments(final URI uri)
   {
      return createProxy(IDocumentsResource.class, uri);
   }

   public IProjectResource getProject(final URI uri)
   {
      return createProxy(IProjectResource.class, uri);
   }

   public IProjectIterationResource getProjectIteration(final URI uri)
   {
      return createProxy(IProjectIterationResource.class, uri);
   }

   public IProjectsResource getProjects(final URI uri)
   {
      return createProxy(IProjectsResource.class, uri);
   }

   public ITranslationResources getTranslationResources(String projectSlug, String versionSlug)
   {
      String spec = "seam/resource/restv1/projects/p/" + projectSlug + "/iterations/i/" + versionSlug + "/r";
      try
      {
         URI uri = new URL(getBase().toURL(), spec).toURI();
         return getTranslationResources(uri);
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

   public ITranslationResources getTranslationResources(final URI uri)
   {
      return createProxy(ITranslationResources.class, uri);
   }

   public <T> T createProxy(Class<T> clazz, URI baseUri)
   {
      log.debug("{} proxy uri: {}", clazz.getSimpleName(), baseUri);
      return super.createProxy(clazz, baseUri);
   }
}

