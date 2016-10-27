package org.zanata.rest.service;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataRestTest;
import org.zanata.common.LocaleId;
import org.zanata.security.ZanataIdentity;
import org.zanata.test.CdiUnitRunner;

@RunWith(CdiUnitRunner.class)
public class TMXDummyRestTest extends ZanataRestTest {

    @Produces
    @Mock
    private ZanataIdentity mockIdentity;

    @Inject
    private TranslationMemoryResource tmService;

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
        this.tmService =
                getClientRequestFactory().createProxy(
                        TranslationMemoryResource.class,
                        createBaseURI("/rest/tm"));
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
//        TranslationMemoryResourceService tmService =
//                seam.autowire(TranslationMemoryResourceService.class);
//        resources.add(seam.autowire(TextFlowStreamingDAO.class));
        resources.add(tmService);
    }

    @Ignore
    @Test
    public void testGetTmx() {
        Response response =
                tmService.getProjectIterationTranslationMemory("iok", "6.4",
                        new LocaleId("as"));
        System.out.println(response.getEntity());
    }

}
