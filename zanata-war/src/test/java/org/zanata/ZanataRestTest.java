package org.zanata;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.InMemoryClientExecutor;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.ResourceFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.zanata.rest.AuthorizationExceptionMapper;
import org.zanata.rest.HibernateExceptionMapper;
import org.zanata.rest.HibernateValidationInterceptor;
import org.zanata.rest.ConstraintViolationExceptionMapper;
import org.zanata.rest.NoSuchEntityExceptionMapper;
import org.zanata.rest.NotLoggedInExceptionMapper;
import org.zanata.rest.ZanataServiceExceptionMapper;
import org.zanata.rest.client.TraceDebugInterceptor;
import org.zanata.seam.SeamAutowire;

public abstract class ZanataRestTest extends ZanataDbunitJpaTest
{

   protected static final URI MOCK_BASE_URI = URI.create("http://mockhost");

   private ClientRequestFactory clientRequestFactory;
   private final SeamAutowire seamAutowire = SeamAutowire.instance();
   protected final Set<Class<? extends ExceptionMapper<? extends Throwable>>> exceptionMappers = new HashSet<Class<? extends ExceptionMapper<? extends Throwable>>>();
   protected final Set<Object> resources = new HashSet<Object>();
   protected final Set<Class<?>> providers = new HashSet<Class<?>>();
   protected final Set<Object> providerInstances = new HashSet<Object>();

   @BeforeMethod
   public final void prepareRestEasyFramework()
   {

      Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
      prepareSeamAutowire();
      prepareResources();
      prepareExceptionMappers();
      prepareProviders();

      // register resources
      for (Object obj : resources)
      {
         ResourceFactory factory = new MockResourceFactory(obj);
         dispatcher.getRegistry().addResourceFactory(factory);
      }

      // register Exception Mappers
      for (Class<? extends ExceptionMapper<? extends Throwable>> mapper : exceptionMappers)
      {
         dispatcher.getProviderFactory().addExceptionMapper(mapper);
      }
      
      // register Providers
      for(Class<?> provider: providers)
      {
         dispatcher.getProviderFactory().registerProvider(provider);
      }

      // register Provider instances
      for(Object providerInstance : providerInstances)
      {
         dispatcher.getProviderFactory().registerProviderInstance(providerInstance);
      }
      
      InMemoryClientExecutor executor = new InMemoryClientExecutor(dispatcher);
      executor.setBaseUri(MOCK_BASE_URI);
      clientRequestFactory = new ClientRequestFactory(executor, MOCK_BASE_URI);

   }

   @AfterMethod
   public final void cleanUpRestEasyFramework()
   {
      exceptionMappers.clear();
      resources.clear();
      providers.clear();
      providerInstances.clear();
   }

   /**
    * Clients should add instances of the tested server side Resource to the
    * protected resources set within this method.
    */
   protected abstract void prepareResources();

   /**
    * Override this to add custom server-side ExceptionMappers for the test, by
    * configuring the protected exceptionMappers set.
    */
   protected void prepareExceptionMappers()
   {
      exceptionMappers.add(AuthorizationExceptionMapper.class);
      exceptionMappers.add(HibernateExceptionMapper.class);
      exceptionMappers.add(ConstraintViolationExceptionMapper.class);
      exceptionMappers.add(NoSuchEntityExceptionMapper.class);
      exceptionMappers.add(NotLoggedInExceptionMapper.class);
      exceptionMappers.add(ZanataServiceExceptionMapper.class);
   }
   
   /**
    * Override this method to add custom server-side Providers for the test.
    * Note: Provider classes should be annotated with the {@link Provider} and any other
    * relevant annotations. 
    */
   protected void prepareProviders()
   {
      ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
      seamAutowire.use("validatorFactory", validatorFactory)
                  .use("validator", validatorFactory.getValidator());
      providerInstances.add(seamAutowire.autowire(HibernateValidationInterceptor.class));
      providers.add(TraceDebugInterceptor.class);
   }

   /**
    * Override this method to add custom Seam autowire preparations.
    */
   protected void prepareSeamAutowire()
   {
      seamAutowire.reset().ignoreNonResolvable();
   }

   /**
    * Retrieve the configured request factory
    * 
    * @return a ClientRequestFactory configured for your environment.
    */
   protected final ClientRequestFactory getClientRequestFactory()
   {
      return clientRequestFactory;
   }

   /**
    * Creates a URI suitable for passing to ClientRequestFactory for a given
    * resource.
    * 
    * @param resourcePath the class-level @Path structure for the resource being
    *           tested.
    * @return a URI suitable for passing to ClientRequestFactory for the
    *         resource being tested.
    */
   protected final URI createBaseURI(String resourcePath)
   {
      return MOCK_BASE_URI.resolve(resourcePath);
   }

   /**
    * Returns this tests SeamAutowire instance.
    *
    * @return This test's SeamAutowire instance to be used in tests.
    */
   protected SeamAutowire getSeamAutowire()
   {
      return seamAutowire;
   }
}
