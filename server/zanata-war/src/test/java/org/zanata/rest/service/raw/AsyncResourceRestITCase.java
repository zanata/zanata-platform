package org.zanata.rest.service.raw;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import static com.jayway.awaitility.Awaitility.waitAtMost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.zanata.test.ResourceTestData.getTestDocWithTextFlow;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.provider.DBUnitProvider;
import org.zanata.provider.DBUnitProvider.DataSetOperation;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.ResourceTestUtil;
import org.zanata.test.TranslationsResourceTestData;
import com.jayway.awaitility.Duration;

/**
 * @Auther pahuang
 */
public class AsyncResourceRestITCase extends RestTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AsyncResourceRestITCase.class);

    private static final String DOCUMENTS_DATA_DBUNIT_XML =
            "org/zanata/test/model/DocumentsData.dbunit.xml";
    private static final String LOCALE_DATA_DBUNIT_XML =
            "org/zanata/test/model/LocalesData.dbunit.xml";
    private static final String PROJECTS_DATA_DBUNIT_XML =
            "org/zanata/test/model/ProjectsData.dbunit.xml";
    private static final String ACCOUNT_DATA_DBUNIT_XML =
            "org/zanata/test/model/AccountData.dbunit.xml";

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(ACCOUNT_DATA_DBUNIT_XML,
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML,
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(PROJECTS_DATA_DBUNIT_XML,
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(LOCALE_DATA_DBUNIT_XML,
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml"));
        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void testPutGetResourceWithExtension() throws Exception {
        final Resource resource = getTestDocWithTextFlow();
        final AtomicReference<String> processId = new AtomicReference<>(null);
        new ResourceRequest(
                getRestEndpointUrl(
                        "async/projects/p/sample-project/iterations/i/1.0/r/"
                                + resource.getName()),
                "PUT", getAuthorizedEnvironment()) {

            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", "gettext").
                        queryParam("ext", "comment").
                        queryParam("copyTrans", false).request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(resource), MediaType.APPLICATION_XML_TYPE);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }


            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(200));
                String entityString = response.readEntity(String.class);

                assertJaxbUnmarshal(entityString, ProcessStatus.class);
                ProcessStatus status =
                        jaxbUnmarshal(entityString, ProcessStatus.class);
                assertThat(status.getUrl(), is(notNullValue()));
                processId.set(status.getUrl());
            }
        }.run();
        waitAtMost(Duration.TEN_SECONDS).catchUncaughtExceptions()
                .pollInterval(Duration.ONE_SECOND)
                .until(asyncPushFinishCallable(processId.get()));
        new ResourceRequest(
                getRestEndpointUrl(
                        "projects/p/sample-project/iterations/i/1.0/r/"
                                + resource.getName()),
                "GET", getAuthorizedEnvironment()) {

            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", "gettext").
                        queryParam("ext", "comment").request();
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(200));
                String entityString = response.readEntity(String.class);
                Resource get = jaxbUnmarshal(entityString, Resource.class);
                Resource base = getTestDocWithTextFlow();
                ResourceTestUtil.clearRevs(base);
                ResourceTestUtil.clearRevs(get);
                log.debug("expect:" + base.toString());
                log.debug("actual:" + get.toString());
                assertThat(get.toString(), is(base.toString()));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void testPutGetTranslationWithExtension() throws Exception {
        final TranslationsResource resource =
                TranslationsResourceTestData.getTestTextFlowTargetComment();
        final AtomicReference<String> processId = new AtomicReference<>(null);
        new ResourceRequest(
                getRestEndpointUrl(
                        "async/projects/p/sample-project/iterations/i/1.0/r/my,path,document.txt/translations/en"),
                "PUT", getAuthorizedEnvironment()) {

            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.queryParam("ext", "gettext").
                        queryParam("ext", "comment").
                        queryParam("merge", "auto").request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(resource),
                                MediaType.APPLICATION_XML_TYPE);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(200));
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, ProcessStatus.class);
                ProcessStatus status =
                        jaxbUnmarshal(entityString, ProcessStatus.class);
                assertThat(status.getUrl(), is(notNullValue()));
                processId.set(status.getUrl());
            }
        }.run();
        waitAtMost(Duration.TEN_SECONDS).catchUncaughtExceptions()
                .pollInterval(Duration.ONE_SECOND)
                .until(asyncPushFinishCallable(processId.get()));
    }

    private Callable<Boolean> asyncPushFinishCallable(final String processId) {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                new ResourceRequest(getRestEndpointUrl("async/" + processId),
                        "GET", getAuthorizedEnvironment()) {

                    @Override
                    protected Invocation.Builder prepareRequest(
                            ResteasyWebTarget webTarget) {
                        return webTarget.request();
                    }

                    @Override
                    protected void onResponse(Response response) {
                        assertThat(response.getStatus(), is(200));
                        String entityString = response.readEntity(String.class);
                        assertJaxbUnmarshal(entityString, ProcessStatus.class);
                        ProcessStatus status =
                                jaxbUnmarshal(entityString, ProcessStatus.class);
                        assertThat(status.getStatusCode(), equalTo(
                                ProcessStatus.ProcessStatusCode.Finished));
                    }
                }.run();
                return true;
            }
        };
    }
}
