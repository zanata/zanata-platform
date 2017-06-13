package org.zanata.rest.service;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataRestTest;
import org.zanata.common.LocaleId;
import org.zanata.jpa.FullText;
import org.zanata.rest.NoSuchEntityException;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.LockManagerService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.Zanata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * TODO: remove deprecated resteasy classes InMemoryClientExecutor and
 * ClientRequestFactory, then remove @Deprecated annotation..
 */
@RunWith(CdiUnitRunner.class)
@SuppressWarnings("deprecation")
public class TMXDummyRestTest extends ZanataRestTest {

    private TranslationMemoryResource tmResource;

    @Inject
    private TranslationMemoryResourceService tmService;

    @Produces
    @Mock
    private ZanataIdentity mockIdentity;

    @Produces
    @Mock
    LocaleService localeService;

    @Produces
    @Mock
    @FullText
    @Zanata
    @Default
    FullTextEntityManager fullTextEntityManager;

    @Produces
    @Mock
    @FullText
    @Zanata
    FullTextSession fullTextSession;

    @Produces
    @Mock
    @Zanata
    SessionFactory sessionFactory;

    @Produces
    @Mock
    LockManagerService lockManagerService;

    @Produces
    @Mock
    UserTransaction userTransaction;

    @Produces
    @Mock
    RestSlugValidator restSlugValidator;

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @BeforeClass
    public static void disableSecurity() {
        ZanataIdentity.setSecurityEnabled(false);
    }

    @Before
    public void createClient() {
        MockitoAnnotations.initMocks(this);
        this.tmResource =
                getClientRequestFactory().createProxy(
                        TranslationMemoryResource.class,
                        createBaseURI("/"));
    }

    @Override
    protected void prepareDBUnitOperations() {
        // empty (not using DBUnit)
    }

    // @Override
    // protected Map<String, String> createPropertiesMap()
    // {
    // return ImmutableMap.of(
    // "hibernate.connection.driver_class", "com.mysql.jdbc.Driver",
    // "hibernate.connection.url",
    // "jdbc:mysql://localhost/refimpl_db?characterEncoding=UTF-8",
    // "hibernate.connection.username", "root",
    // "hibernate.connection.password", "",
    // "hibernate.dialect", "org.zanata.util.ZanataMySQL5InnoDBDialect");
    // }

    @Override
    protected void prepareResources() {
        resources.add(tmService);
    }

    @Test
    @InRequestScope
    public void testGetTmxForMissingProject() throws Exception {
        when(restSlugValidator.retrieveAndCheckIteration("iok", "6.4", false))
                .thenThrow(new NoSuchEntityException("Project \'iok\' not found."));
        // TODO find a way to get entity without ClientResponse
        @SuppressWarnings("unchecked")
        // don't import this class, because @SuppressWarnings("deprecation") won't work
        org.jboss.resteasy.client.ClientResponse<String> response = (org.jboss.resteasy.client.ClientResponse<String>)
                tmResource.getProjectIterationTranslationMemory("iok", "6.4",
                        new LocaleId("as"));
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getEntity(String.class)).isEqualTo("Project \'iok\' not found.");
    }

    @Ignore
    @Test
    @InRequestScope
    public void testGetTmx() throws Exception {
//        HProject project = new HProject();
//        when(restSlugValidator.retrieveAndCheckProject("iok", false)).thenReturn(project);
        // TODO get this working with a mock Session
//        when(sessionFactory.openSession()).

        @SuppressWarnings("unchecked")
        org.jboss.resteasy.client.ClientResponse<String> response = (org.jboss.resteasy.client.ClientResponse<String>)
                tmResource.getProjectIterationTranslationMemory("iok", "6.4",
                        new LocaleId("as"));
        assertThat(response.getStatus()).isEqualTo(200);

//        InputStream inputStream = response.readEntity(InputStream.class);
//        System.out.println(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
    }

}
