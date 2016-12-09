package org.zanata.rest.service;

import static org.mockito.Answers.RETURNS_SMART_NULLS;
import static org.mockito.Mockito.when;
import static org.zanata.util.CloseableIterator.closeable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.jboss.resteasy.client.ClientResponse;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataRestTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowStreamingDAO;
import org.zanata.dao.TransMemoryDAO;
import org.zanata.dao.TransMemoryStreamingDAO;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.LockManagerService;
import org.zanata.test.CdiUnitRunner;
import org.zanata.tmx.TransMemoryAdapter;
import org.zanata.util.CloseableIterator;

@RunWith(CdiUnitRunner.class)
public class TMXDummyRestTest extends ZanataRestTest {

    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    private ZanataIdentity mockIdentity;

    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    TransMemoryAdapter tmAdapter;

    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    LocaleService localeService;

    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    LockManagerService lockManagerServiceImpl;

    @Produces
    @Mock
    TextFlowStreamingDAO textFlowStreamDAO;

    @Produces
    @Mock
    TransMemoryStreamingDAO transMemoryStreamingDAO;

    @Produces
    @Mock
    TransMemoryDAO transMemoryDAO;

    @Produces
    @Mock(answer = RETURNS_SMART_NULLS)
    RestSlugValidator restSlugValidator;

    @Inject
    private TranslationMemoryResourceService tmService;

    // our proxy for invoking the mock REST resource
    private TranslationMemoryResource tmResource;
    private HProjectIteration projVer;

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
        this.tmResource = getClientRequestFactory()
                .createProxy(TranslationMemoryResource.class, "");
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

    // TODO: To run, this test needs more mock interactions defined.
    // TODO: Then it needs to include assertions about the result.
    @Ignore
    @Test
    @InRequestScope
    public void testGetTmx() throws IOException {
        projVer = new HProjectIteration();
        // TODO set up test data
        Collection<HTextFlow> textFlows = Arrays.asList();
        CloseableIterator<HTextFlow> textFlowIter = closeable(textFlows.iterator());
        when(this.textFlowStreamDAO.findTextFlowsByProjectIteration(projVer)).thenReturn(textFlowIter);
        ClientResponse<InputStream> response =
            (ClientResponse<InputStream>) tmResource.getProjectIterationTranslationMemory("iok", "6.4",
                    new LocaleId("as"));
        String entity = IOUtils.toString(response.getEntity(InputStream.class), StandardCharsets.UTF_8);
        System.out.println(entity);
    }

}
