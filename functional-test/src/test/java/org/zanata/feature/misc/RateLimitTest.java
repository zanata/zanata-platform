package org.zanata.feature.misc;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.administration.AdministrationPage;
import org.zanata.page.administration.ServerConfigurationPage;
import org.zanata.util.AddUsersRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
@Slf4j
public class RateLimitTest {
    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    private static final String TRANSLATOR = "translator";
    private static final String TRANSLATOR_API =
            "d83882201764f7d339e97c4b087f0806";

    @Test
    public void canConfigureRateLimit() {
        new LoginWorkFlow().signIn("admin", "admin");
        BasicWorkFlow basicWorkFlow = new BasicWorkFlow();
        ServerConfigurationPage serverConfigPage =
                basicWorkFlow.goToPage("admin/server_configuration",
                        ServerConfigurationPage.class);

        assertThat(serverConfigPage.getRateLimit(), Matchers.isEmptyString());

        AdministrationPage administrationPage =
                serverConfigPage.inputRateLimit(1).save();

        assertThat(administrationPage.getNotificationMessage(),
                Matchers.equalTo("Configuration was successfully updated."));

        serverConfigPage =
                basicWorkFlow.goToPage("admin/server_configuration",
                        ServerConfigurationPage.class);
        assertThat(serverConfigPage.getRateLimit(), Matchers.equalTo("1"));
    }

    @Test
    public void canRateLimitRestRequestsPerAPIKey() throws InterruptedException {
        new LoginWorkFlow().signIn("admin", "admin");
        BasicWorkFlow basicWorkFlow = new BasicWorkFlow();
        ServerConfigurationPage serverConfigPage =
                basicWorkFlow.goToPage("admin/server_configuration",
                        ServerConfigurationPage.class);
        // adjust rate limit per second
        serverConfigPage.inputRateLimit(3).save();

        // prepare to fire multiple REST requests
        final AtomicInteger atomicInteger = new AtomicInteger(1);
        // translator creates the project/version
        final String projectSlug = "project";
        final String iterationSlug = "version";
        new ZanataRestCaller(TRANSLATOR, TRANSLATOR_API)
                .createProjectAndVersion(projectSlug, iterationSlug, "gettext");

        // requests from translator user
        final int translatorThreads = 4;
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
        int adminThreads = 3;
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

        // 1 request from translator should get 503 and fail
        log.info("result: {}", result);
        assertThat(result, Matchers.containsInAnyOrder(201, 201, 201, 201, 201,
                201, 403));
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

    private static List<Integer> getResultStatusCodes(List<Future<Integer>> futures) {
        return Lists.transform(futures,
                new Function<Future<Integer>, Integer>() {
                    @Override
                    public Integer apply(Future<Integer> input) {
                        try {
                            return input.get();
                        }
                        catch (Exception e) {
                            throw Throwables.propagate(e);
                        }
                    }
                });
    }
}
