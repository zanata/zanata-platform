package org.zanata;

import java.net.URI;
import java.util.Set;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.Session;
import org.jboss.resteasy.client.jaxrs.ProxyBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.config.SystemPropertyConfigStore;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.rest.AuthorizationExceptionMapper;
import org.zanata.rest.HibernateExceptionMapper;
import org.zanata.rest.ConstraintViolationExceptionMapper;
import org.zanata.rest.NoSuchEntityExceptionMapper;
import org.zanata.rest.NotLoggedInExceptionMapper;
import org.zanata.rest.ZanataServiceExceptionMapper;
import org.zanata.security.AuthenticationType;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.when;

/**
 * TODO: remove deprecated resteasy classes InMemoryClientExecutor and
 * ClientRequestFactory
 */
@SuppressWarnings("deprecation")
public abstract class ZanataRestTest extends ZanataDbunitJpaTest {

    protected static final URI MOCK_BASE_URI = URI.create("http://mockhost");
    private ResteasyClientBuilder resteasyClientBuilder;
    // don't import this class, because @SuppressWarnings("deprecation") won't work
    private org.jboss.resteasy.client.ClientRequestFactory clientRequestFactory;
    protected final Set<Class<? extends ExceptionMapper<? extends Throwable>>> exceptionMappers =
            newHashSet();
    protected final Set<Object> resources = newHashSet();
    protected final Set<Class<?>> providers = newHashSet();
    protected final Set<Object> providerInstances = newHashSet();
    @Mock
    private SystemPropertyConfigStore systemPropertyConfigStore;

    @Before
    public void prepareRestEasyFramework() {
        MockitoAnnotations.initMocks(this);
        when(systemPropertyConfigStore.getEnabledAuthenticationPolicies())
                .thenReturn(
                        newHashSet(AuthenticationType.INTERNAL.name()));
        Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
        prepareAccount();
        prepareResources();
        prepareExceptionMappers();
        prepareProviders();

        // register resources
        for (Object obj : resources) {
            dispatcher.getRegistry().addSingletonResource(obj);
        }

        // register Exception Mappers
        for (Class<? extends ExceptionMapper<? extends Throwable>> mapper : exceptionMappers) {
            dispatcher.getProviderFactory().registerProvider(mapper);
        }

        // register Providers
        for (Class<?> provider : providers) {
            dispatcher.getProviderFactory().registerProvider(provider);
        }

        // register Provider instances
        for (Object providerInstance : providerInstances) {
            dispatcher.getProviderFactory().registerProviderInstance(
                    providerInstance);
        }

        org.jboss.resteasy.client.core.executors.InMemoryClientExecutor executor =
                new org.jboss.resteasy.client.core.executors.InMemoryClientExecutor(dispatcher);
        executor.setBaseUri(MOCK_BASE_URI);
        clientRequestFactory =
                new org.jboss.resteasy.client.ClientRequestFactory(executor, MOCK_BASE_URI);

    }

    private void prepareAccount() {
        Session session = getSession();
        String username = "anAuthenticatedAccount";
        HAccount account =
                session.byNaturalId(HAccount.class).using("username", username)
                        .load();
        if (account == null) {
            account = new HAccount();
            account.setUsername(username);
            HPerson person = new HPerson();
            person.setEmail("email@example.com");
            person.setName("aPerson");
            person = (HPerson) session.merge(person);
            account.setPerson(person);
            account = (HAccount) session.merge(account);
        }
    }

    @After
    public void cleanUpRestEasyFramework() {
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
    protected void prepareExceptionMappers() {
        exceptionMappers.add(AuthorizationExceptionMapper.class);
        exceptionMappers.add(HibernateExceptionMapper.class);
        exceptionMappers.add(ConstraintViolationExceptionMapper.class);
        exceptionMappers.add(NoSuchEntityExceptionMapper.class);
        exceptionMappers.add(NotLoggedInExceptionMapper.class);
        exceptionMappers.add(ZanataServiceExceptionMapper.class);
    }

    /**
     * Override this method to add custom server-side Providers for the test.
     * Note: Provider classes should be annotated with the {@link Provider} and
     * any other relevant annotations.
     */
    protected void prepareProviders() {
    }

    /**
     * Retrieve the configured request factory
     *
     * @return a ClientRequestFactory configured for your environment.
     */
    protected org.jboss.resteasy.client.ClientRequestFactory getClientRequestFactory() {
        return clientRequestFactory;
    }

    /**
     * This is to replace {@link #getClientRequestFactory()}
     */
    protected <T> ProxyBuilder<T> createProxy(Class<T> clazz, URI uri) {
        ResteasyWebTarget webTarget = resteasyClientBuilder.build().target(uri);
        return ProxyBuilder.builder(clazz, webTarget);
    }

    /**
     * Creates a URI suitable for passing to ClientRequestFactory for a given
     * resource.
     *
     * @param resourcePath
     *            the class-level @Path structure for the resource being tested.
     * @return a URI suitable for passing to ClientRequestFactory for the
     *         resource being tested.
     */
    protected URI createBaseURI(String resourcePath) {
        return MOCK_BASE_URI.resolve(resourcePath);
    }
}
