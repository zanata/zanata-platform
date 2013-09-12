package org.zanata.rest;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Collection;

import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.UnhandledException;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.deployment.AnnotationDeploymentHandler;
import org.jboss.seam.deployment.HotDeploymentStrategy;
import org.jboss.seam.log.Log;
import org.jboss.seam.resteasy.ResteasyBootstrap;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;
import org.zanata.seam.resteasy.IgnoreInterfacePath;

@Name("org.jboss.seam.resteasy.bootstrap")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
@Install(classDependencies = "org.jboss.resteasy.spi.ResteasyProviderFactory", precedence = Install.DEPLOYMENT)
public class ZanataResteasyBootstrap extends ResteasyBootstrap
{

   @Logger
   Log log;

   @Observer("org.jboss.seam.postReInitialization")
   public void registerHotDeployedClasses()
   {

      Collection<Component> seamComponents = findSeamComponents();

      // Also scan for hot deployed components
      HotDeploymentStrategy hotDeployment = HotDeploymentStrategy.instance();
      if (hotDeployment != null && hotDeployment.available())
      {
         log.info("scanning for hot deployable JAX-RS components");
         AnnotationDeploymentHandler hotDeploymentHandler = (AnnotationDeploymentHandler) hotDeployment.getDeploymentHandlers().get(AnnotationDeploymentHandler.NAME);
         registerProviders(seamComponents, findProviders(hotDeploymentHandler));
         registerResources(seamComponents, findResources(hotDeploymentHandler));
      }
   }

   @Override
   protected void initDispatcher()
   {
      super.initDispatcher();
      getDispatcher().getProviderFactory().getServerPreProcessInterceptorRegistry().register(ZanataRestSecurityInterceptor.class);
      getDispatcher().getProviderFactory().getServerPreProcessInterceptorRegistry().register(ZanataRestVersionInterceptor.class);
   }

   @Override
   protected Dispatcher createDispatcher(SeamResteasyProviderFactory providerFactory)
   {
      return new SynchronousDispatcher(providerFactory)
      {
         @Override
         public void invoke(HttpRequest request, HttpResponse response)
         {
            try
            {
               super.invoke(request, response);
            }
            catch (UnhandledException e)
            {
               log.error("Failed to process REST request", e.getCause());
               try
               {
                  response.sendError(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "Error processing Request");
               }
               catch (IOException ioe)
               {
                  log.error("Failed to send error on failed REST request", ioe);
               }
            }
         }
      };
   }

   /**
    * If the seam bean is annotated with @IgnoreInterfacePath, any @Path
    * annotation on the bean's interfaces will be ignored when deciding whether
    * to inject @Context variables based on the interface class or the bean
    * class.
    * @param annotation
    * @param seamComponent
    * @return
    */
   @Override
   protected Class getAnnotatedInterface(Class<? extends Annotation> annotation,
                                         Component seamComponent)
   {
      if (annotation == javax.ws.rs.Path.class &&
         seamComponent.getBeanClass().isAnnotationPresent(
            IgnoreInterfacePath.class))
      {
         return null;
      }
      else
      {
         return super.getAnnotatedInterface(annotation, seamComponent);
      }
   }

}
