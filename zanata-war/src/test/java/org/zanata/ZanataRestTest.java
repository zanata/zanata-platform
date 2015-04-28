package org.zanata;

import java.net.URI;
import java.util.Set;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.Session;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.InMemoryClientExecutor;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.seam.security.management.JpaIdentityStore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.zanata.config.JndiBackedConfig;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.rest.AuthorizationExceptionMapper;
import org.zanata.rest.HibernateExceptionMapper;
import org.zanata.rest.HibernateValidationInterceptor;
import org.zanata.rest.ConstraintViolationExceptionMapper;
import org.zanata.rest.NoSuchEntityExceptionMapper;
import org.zanata.rest.NotLoggedInExceptionMapper;
import org.zanata.rest.ZanataServiceExceptionMapper;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.AuthenticationType;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.when;

public abstract class ZanataRestTest extends ZanataDbunitJpaTest {

    protected static final URI MOCK_BASE_URI = URI.create("http://mockhost");

    private ClientRequestFactory clientRequestFactory;
    private static final SeamAutowire seamAutowire = SeamAutowire.instance();
    protected final Set<Class<? extends ExceptionMapper<? extends Throwable>>> exceptionMappers =
            newHashSet();
    protected final Set<Object> resources = newHashSet();
    protected final Set<Class<?>> providers = newHashSet();
    protected final Set<Object> providerInstances = newHashSet();
    @Mock
    private JndiBackedConfig jndiBackedConfig;

    @BeforeMethod
    public final void prepareRestEasyFramework() {
        MockitoAnnotations.initMocks(this);
        when(jndiBackedConfig.getEnabledAuthenticationPolicies()).thenReturn(
                newHashSet(AuthenticationType.INTERNAL.name()));
        Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
        prepareSeamAutowire();
        prepareAccount();
        prepareResources();
        prepareExceptionMappers();
        prepareProviders();

        // register resources
        for (Object obj : resources) {
            ResourceFactory factory = new MockResourceFactory(obj);
            dispatcher.getRegistry().addResourceFactory(factory);
        }

        // register Exception Mappers
        for (Class<? extends ExceptionMapper<? extends Throwable>> mapper : exceptionMappers) {
            dispatcher.getProviderFactory().addExceptionMapper(mapper);
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

        InMemoryClientExecutor executor =
                new InMemoryClientExecutor(dispatcher);
        executor.setBaseUri(MOCK_BASE_URI);
        clientRequestFactory =
                new ClientRequestFactory(executor, MOCK_BASE_URI);

    }

    private void prepareAccount() {
        Session session = getSession();
        String username = "anAuthenticatedAccount";
        HAccount account = (HAccount) session.byNaturalId(HAccount.class).using("username", username).load();
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
        seamAutowire.use(JpaIdentityStore.AUTHENTICATED_USER, account);
    }

    @AfterMethod
    public final void cleanUpRestEasyFramework() {
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
        ValidatorFactory validatorFactory =
                Validation.buildDefaultValidatorFactory();
        seamAutowire.use("validatorFactory", validatorFactory).use("validator",
                validatorFactory.getValidator());
        providerInstances.add(seamAutowire
                .autowire(HibernateValidationInterceptor.class));
    }

    /**
     * Override this method to add custom Seam autowire preparations.
     */
    protected void prepareSeamAutowire() {
        seamAutowire
                .reset()
                .ignoreNonResolvable();
    }

    /**
     * Retrieve the configured request factory
     *
     * @return a ClientRequestFactory configured for your environment.
     */
    protected final ClientRequestFactory getClientRequestFactory() {
        return clientRequestFactory;
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
    protected final URI createBaseURI(String resourcePath) {
        return MOCK_BASE_URI.resolve(resourcePath);
    }

    /**
     * Returns this tests SeamAutowire instance.
     *
     * @return This test's SeamAutowire instance to be used in tests.
     */
    protected SeamAutowire getSeamAutowire() {
        return seamAutowire;
    }
}
