package net.openl10n.flies.rest.client;

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

public class FliesClientRequestFactory implements ITranslationResourcesFactory
{
   static
   {
      ResteasyProviderFactory instance = ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(instance);
   }

   private static final Logger log = LoggerFactory.getLogger(FliesClientRequestFactory.class);
   private final ClientRequestFactory crf;

   public FliesClientRequestFactory(String username, String apiKey)
   {
      this(null, username, apiKey);
   }

   public FliesClientRequestFactory(URI base, String username, String apiKey)
   {
      crf = new ClientRequestFactory(fixBase(base));
      crf.getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
   }

   public FliesClientRequestFactory(URI base, String username, String apiKey, ClientExecutor executor)
   {
      crf = new ClientRequestFactory(executor, null, fixBase(base));
      crf.getPrefixInterceptors().registerInterceptor(new ApiKeyHeaderDecorator(username, apiKey));
   }

   private <T> T createProxy(Class<T> clazz, URI baseUri)
   {
      log.debug("{} proxy uri: {}", clazz.getSimpleName(), baseUri);
      return crf.createProxy(clazz, baseUri);
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

   public IAccountResource getAccount(String username)
   {
      return getAccount(getAccountURI(username));
   }

   private IAccountResource getAccount(final URI uri)
   {
      return createProxy(IAccountResource.class, uri);
   }

   public URI getAccountURI(String username)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), "seam/resource/restv1/accounts/u/" + username);
         return url.toURI();
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

   public IDocumentsResource getDocuments(final URI uri)
   {
      return createProxy(IDocumentsResource.class, uri);
   }

   public URI getDocumentsURI(String proj, String iter)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), "seam/resource/restv1/projects/p/" + proj + "/iterations/i/" + iter + "/documents");
         return url.toURI();
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

   public IProjectResource getProject(String proj)
   {
      return getProject(getProjectURI(proj));
   }

   private IProjectResource getProject(final URI uri)
   {
      return createProxy(IProjectResource.class, uri);
   }

   public URI getProjectURI(String proj)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), "seam/resource/restv1/projects/p/" + proj);
         return url.toURI();
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

   public IProjectIterationResource getProjectIteration(String proj, String iter)
   {
      return getProjectIteration(getProjectIterationURI(proj, iter));
   }

   public IProjectIterationResource getProjectIteration(final URI uri)
   {
      return createProxy(IProjectIterationResource.class, uri);
   }

   public URI getProjectIterationURI(String proj, String iter)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), "seam/resource/restv1/projects/p/" + proj + "/iterations/i/" + iter);
         return url.toURI();
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

   // NB IProjectsResource is not currently used in Java
   public IProjectsResource getProjects(final URI uri)
   {
      return createProxy(IProjectsResource.class, uri);
   }

   @Override
   public ITranslationResources getTranslationResources(String projectSlug, String versionSlug)
   {
      return getTranslationResources(getTranslationResourcesURI(projectSlug, versionSlug));
   }

   private ITranslationResources getTranslationResources(final URI uri)
   {
      return createProxy(ITranslationResources.class, uri);
   }

   @Override
   public URI getTranslationResourcesURI(String projectSlug, String versionSlug)
   {
      String spec = "seam/resource/restv1/projects/p/" + projectSlug + "/iterations/i/" + versionSlug + "/r";
      try
      {
         return new URL(crf.getBase().toURL(), spec).toURI();
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

}

