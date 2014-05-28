package org.zanata.feature.misc;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.administration.ServerConfigurationPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.model.HApplicationConfiguration.KEY_ADMIN_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_MAX_ACTIVE_REQ_PER_API_KEY;
import static org.zanata.model.HApplicationConfiguration.KEY_MAX_CONCURRENT_REQ_PER_API_KEY;
import static org.zanata.util.ZanataRestCaller.checkStatusAndReleaseConnection;
import static org.zanata.util.ZanataRestCaller.getStatusAndReleaseConnection;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
@Slf4j
public class RateLimitRestAndUITest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    private static final String TRANSLATOR = "translator";
    private static final String TRANSLATOR_API =
            "d83882201764f7d339e97c4b087f0806";
    private String maxConcurrentPathParam = "c/"
            + KEY_MAX_CONCURRENT_REQ_PER_API_KEY;
    private String maxActivePathParam = "c/" + KEY_MAX_ACTIVE_REQ_PER_API_KEY;

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canConfigureRateLimitByWebUI() {
        new LoginWorkFlow().signIn("admin", "admin");
        BasicWorkFlow basicWorkFlow = new BasicWorkFlow();
        ServerConfigurationPage serverConfigPage =
                basicWorkFlow.goToPage("admin/server_configuration",
                        ServerConfigurationPage.class);

        assertThat(serverConfigPage.getMaxConcurrentRequestsPerApiKey(),
                Matchers.equalTo("default is 6"));
        assertThat(serverConfigPage.getMaxActiveRequestsPerApiKey(),
                Matchers.equalTo("default is 2"));

        AdministrationPage administrationPage =
                serverConfigPage.inputMaxConcurrent(5).inputMaxActive(3).save();

        assertThat(administrationPage.getNotificationMessage(),
                Matchers.equalTo("Configuration was successfully updated."));

        serverConfigPage =
                basicWorkFlow.goToPage("admin/server_configuration",
                        ServerConfigurationPage.class);
        assertThat(serverConfigPage.getMaxActiveRequestsPerApiKey(),
                Matchers.equalTo("3"));
        assertThat(serverConfigPage.getMaxConcurrentRequestsPerApiKey(),
                Matchers.equalTo("5"));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canCallServerConfigurationRestService() throws Exception {
        ClientRequest clientRequest =
                clientRequestAsAdmin("rest/configurations/"
                        + maxConcurrentPathParam);
        clientRequest.body("text/plain", "1");
        // can put
        Response putResponse = clientRequest.put();

        assertThat(getStatusAndReleaseConnection(putResponse), Matchers.is(201));

        // can get single configuration
        Response getResponse =
                clientRequestAsAdmin(
                        "rest/configurations/" + maxConcurrentPathParam).get();

        assertThat(getResponse.getStatus(), Matchers.is(200));
        String rateLimitConfig =
                ((BaseClientResponse<String>) getResponse)
                        .getEntity(String.class);
        assertThat(rateLimitConfig,
                Matchers.containsString(KEY_MAX_CONCURRENT_REQ_PER_API_KEY));
        assertThat(rateLimitConfig, Matchers.containsString("<value>1</value>"));

        // can get all configurations
        Response getAllResponse =
                clientRequestAsAdmin("rest/configurations/").get();
        BaseClientResponse<String> baseClientResponse =
                (BaseClientResponse) getAllResponse;

        String configurations = baseClientResponse.getEntity(String.class);
        log.info("result {}", configurations);

        assertThat(getStatusAndReleaseConnection(getAllResponse),
                Matchers.is(200));
        assertThat(configurations, Matchers.notNullValue());
    }

    private static ClientRequest clientRequestAsAdmin(String path) {
        ClientRequest clientRequest =
                new ClientRequest(
                        PropertiesHolder.getProperty(Constants.zanataInstance
                                .value()) + path);
        clientRequest.header("X-Auth-User", "admin");
        clientRequest.header("X-Auth-Token",
                PropertiesHolder.getProperty(Constants.zanataApiKey.value()));
        clientRequest.header("Content-Type", "application/xml");
        return clientRequest;
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void serverConfigurationRestServiceOnlyAvailableToAdmin()
            throws Exception {
        // all request should be rejected
        Response response =
                clientRequestAsTranslator("rest/configurations/").get();
        assertThat(getStatusAndReleaseConnection(response), Matchers.is(401));

        Response response1 =
                clientRequestAsTranslator(
                        "rest/configurations/c/" + KEY_ADMIN_EMAIL).get();
        assertThat(getStatusAndReleaseConnection(response1), Matchers.is(401));

        ClientRequest request =
                clientRequestAsTranslator("rest/configurations/c/"
                        + KEY_ADMIN_EMAIL);
        request.body("text/plain", "admin@email.com");
        Response response2 = request.put();
        assertThat(getStatusAndReleaseConnection(response2), Matchers.is(401));
    }

    private static ClientRequest clientRequestAsTranslator(String path) {
        ClientRequest clientRequest =
                new ClientRequest(
                        PropertiesHolder.getProperty(Constants.zanataInstance
                                .value()) + path);
        clientRequest.header("X-Auth-User", TRANSLATOR);
        clientRequest.header("X-Auth-Token", TRANSLATOR_API);
        clientRequest.header("Content-Type", "application/xml");
        return clientRequest;
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canOnlyDealWithKnownConfiguration() throws Exception {
        ClientRequest clientRequest =
                clientRequestAsAdmin("rest/configurations/c/abc");

        Response putResponse = clientRequest.put();
        assertThat(getStatusAndReleaseConnection(putResponse), Matchers.is(400));

        Response getResponse =
                clientRequestAsAdmin("rest/configurations/c/abc").get();
        assertThat(getStatusAndReleaseConnection(getResponse), Matchers.is(404));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void canLimitConcurrentRestRequestsPerAPIKey() throws Exception {
        // translator creates the project/version
        final String projectSlug = "project";
        final String iterationSlug = "version";
        new ZanataRestCaller(TRANSLATOR, TRANSLATOR_API)
                .createProjectAndVersion(projectSlug, iterationSlug, "gettext");

        ClientRequest clientRequest =
                clientRequestAsAdmin("rest/configurations/"
                        + maxConcurrentPathParam);
        clientRequest.body(MediaType.TEXT_PLAIN_TYPE, "2");

        checkStatusAndReleaseConnection(clientRequest.put());

        // prepare to fire multiple REST requests
        final AtomicInteger atomicInteger = new AtomicInteger(1);

        // requests from translator user
        final int translatorThreads = 3;
        Callable<Integer> translatorTask = new Callable<Integer>() {

            @Override
            public Integer call() {
                return invokeRestService(new ZanataRestCaller(TRANSLATOR,
                        TRANSLATOR_API), projectSlug, iterationSlug,
                        atomicInteger);
            }
        };
        List<Callable<Integer>> translatorTasks =
                Collections.nCopies(translatorThreads, translatorTask);

        // requests from admin user
        int adminThreads = 2;
        Callable<Integer> adminTask = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return invokeRestService(new ZanataRestCaller(), projectSlug,
                        iterationSlug, atomicInteger);
            }
        };

        List<Callable<Integer>> adminTasks =
                Collections.nCopies(adminThreads, adminTask);

        ExecutorService executorService =
                Executors.newFixedThreadPool(translatorThreads + adminThreads);

        List<Callable<Integer>> tasks =
                ImmutableList.<Callable<Integer>> builder()
                        .addAll(translatorTasks).addAll(adminTasks).build();

        List<Future<Integer>> futures = executorService.invokeAll(tasks);

        List<Integer> result = getResultStatusCodes(futures);

        // 1 request from translator should get 403 and fail
        log.info("result: {}", result);
        assertThat(result, Matchers.containsInAnyOrder(201, 201, 201, 201, 429));
    }

    @Test(timeout = 5000)
    public void exceptionWillReleaseSemaphore() throws Exception {
        // Given: max active is set to 1
        ClientRequest configRequest =
                clientRequestAsAdmin("rest/configurations/"
                        + maxActivePathParam);
        configRequest.body(MediaType.TEXT_PLAIN_TYPE, "1");
        checkStatusAndReleaseConnection(configRequest.put());

        // When: multiple requests that will result in a mapped exception
        ClientRequest clientRequest =
                clientRequestAsAdmin("rest/test/data/sample/dummy?exception=org.zanata.rest.NoSuchEntityException");
        getStatusAndReleaseConnection(clientRequest.get());
        getStatusAndReleaseConnection(clientRequest.get());
        getStatusAndReleaseConnection(clientRequest.get());
        getStatusAndReleaseConnection(clientRequest.get());

        // Then: request that result in exception should still release
        // semaphore. i.e. no permit leak
        assertThat(1, Matchers.is(1));
    }

    @Test(timeout = 5000)
    public void unmappedExceptionWillAlsoReleaseSemaphore() throws Exception {
        // Given: max active is set to 1
        ClientRequest configRequest =
                clientRequestAsAdmin("rest/configurations/"
                        + maxActivePathParam);
        configRequest.body(MediaType.TEXT_PLAIN_TYPE, "1");
        checkStatusAndReleaseConnection(configRequest.put());

        // When: multiple requests that will result in an unmapped exception
        ClientRequest clientRequest =
                clientRequestAsAdmin("rest/test/data/sample/dummy?exception=java.lang.RuntimeException");
        getStatusAndReleaseConnection(clientRequest.get());
        getStatusAndReleaseConnection(clientRequest.get());
        getStatusAndReleaseConnection(clientRequest.get());
        getStatusAndReleaseConnection(clientRequest.get());

        // Then: request that result in exception should still release
        // semaphore. i.e. no permit leak
        assertThat(1, Matchers.is(1));
    }

    private static Integer invokeRestService(ZanataRestCaller restCaller,
            String projectSlug, String iterationSlug,
            AtomicInteger atomicInteger) {
        try {
            int counter = atomicInteger.getAndIncrement();
            return restCaller.postSourceDocResource(projectSlug, iterationSlug,
                    ZanataRestCaller.buildSourceResource("doc" + counter,
                            ZanataRestCaller.buildTextFlow("res" + counter,
                                    "content" + counter)), false);
        } catch (Exception e) {
            log.info("rest call failed: {}", e.getMessage());
            return 500;
        }
    }

    private static List<Integer> getResultStatusCodes(
            List<Future<Integer>> futures) {
        return Lists.transform(futures,
                new Function<Future<Integer>, Integer>() {
                    @Override
                    public Integer apply(Future<Integer> input) {
                        try {
                            return input.get();
                        } catch (Exception e) {
                            // by using filter we lose RESTeasy's exception
                            // translation
                            String message = e.getMessage().toLowerCase();
                            if (message
                                    .matches(".+429.+too many concurrent request.+")) {
                                return 429;
                            }
                            throw Throwables.propagate(e);
                        }
                    }
                });
    }
}
