package org.zanata.rest.editor.service;

import java.util.List;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.dao.ActivityDAO;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.rest.editor.dto.Locale;
import org.zanata.seam.SeamAutowire;
import org.zanata.service.impl.ActivityServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class LocalesServiceTest extends ZanataDbunitJpaTest {

    private Response okResponse;
    private Response response;


    private SeamAutowire seam = SeamAutowire.instance();

    private LocalesService localesService;

    /**
     * Implement this in a subclass.
     * <p/>
     * Use it to stack DBUnit <tt>DataSetOperation</tt>'s with the
     * <tt>beforeTestOperations</tt> and <tt>afterTestOperations</tt> lists.
     */
    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @BeforeMethod
    public void initializeSeam() {
        seam.reset().useImpl(LocaleServiceImpl.class)
                .use("session", getSession()).ignoreNonResolvable();
        localesService = seam.autowire(LocalesService.class);

        okResponse = Response.ok().build();
    }

    @AfterMethod
    public void afterMethod() {
        okResponse = null;
        response = null;
    }

    @Test
    public void testGetLocales() {
        response = localesService.get();
        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

}
