package net.openl10n.flies.rest.client;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.openl10n.flies.rest.RestConstant;
import net.openl10n.flies.rest.client.IAccountResource;
import net.openl10n.flies.rest.client.IProjectIterationResource;
import net.openl10n.flies.rest.client.IProjectResource;
import net.openl10n.flies.rest.client.IProjectsResource;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.client.IVersion;
import net.openl10n.flies.rest.dto.VersionInfo;

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
   private static final String RESOURCE_PREFIX = "rest";

   public FliesClientRequestFactory(String username, String apiKey, VersionInfo clientApiVersion)
   {
      this(null, username, apiKey, clientApiVersion);
   }

   public FliesClientRequestFactory(URI base, String username, String apiKey, VersionInfo clientApiVersion)
   {
      this(base, username, apiKey, null, clientApiVersion);
   }

   public FliesClientRequestFactory(URI base, String username, String apiKey, ClientExecutor executor, VersionInfo clientApiVersion)
   {
      crf = new ClientRequestFactory(executor, null, fixBase(base));
      registerPrefixInterceptor(new ApiKeyHeaderDecorator(username, apiKey, clientApiVersion.getVersionNo()));
      String clientVer = clientApiVersion.getVersionNo();
      String clientTimestamp = clientApiVersion.getBuildTimeStamp();
      IVersion iversion = createIVersion();
      VersionInfo serverVersionInfo = iversion.get();
      String serverVer = serverVersionInfo.getVersionNo();
      String serverTimestamp = serverVersionInfo.getBuildTimeStamp();

      log.info("Flies client API version: {}, Flies server API version: {}", clientVer, serverVer);
      if (!serverVer.equals(clientVer))
      {
         log.warn("client API version is {}, but server API version is {}", clientVer, serverVer);
      }
      else if (serverVer.contains(RestConstant.SNAPSHOT_VERSION) && !serverTimestamp.equalsIgnoreCase(clientTimestamp))
      {
         log.warn("client API timestamp is {}, but server API timestamp is {}", clientTimestamp, serverTimestamp);
      }
   }

   public <T> T createProxy(Class<T> clazz, URI baseUri)
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
      return createProxy(IAccountResource.class, getAccountURI(username));
   }

   public URI getAccountURI(String username)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/accounts/u/" + username);
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
      return createProxy(IProjectResource.class, getProjectURI(proj));
   }


   public URI getProjectURI(String proj)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/projects/p/" + proj);
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
      return createProxy(IProjectIterationResource.class, getProjectIterationURI(proj, iter));
   }

   public URI getProjectIterationURI(String proj, String iter)
   {
      try
      {
         URL url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/projects/p/" + proj + "/iterations/i/" + iter);
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
      return createProxy(ITranslationResources.class, getTranslationResourcesURI(projectSlug, versionSlug));
   }


   @Override
   public URI getTranslationResourcesURI(String projectSlug, String versionSlug)
   {
      String spec = RESOURCE_PREFIX + "/projects/p/" + projectSlug + "/iterations/i/" + versionSlug + "/r";
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

   /**
    * @see org.jboss.resteasy.client.core.ClientInterceptorRepositoryImpl#registerInterceptor(Object)
    * @param interceptor
    */
   public void registerPrefixInterceptor(Object interceptor)
   {
      crf.getPrefixInterceptors().registerInterceptor(interceptor);
   }
   
   private IVersion createIVersion()
   {
      URL url;
      try
      {
         url = new URL(crf.getBase().toURL(), RESOURCE_PREFIX + "/version");
         return createProxy(IVersion.class, url.toURI());
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

