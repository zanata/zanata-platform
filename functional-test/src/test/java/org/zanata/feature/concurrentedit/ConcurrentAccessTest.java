package org.zanata.feature.concurrentedit;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.AddUsersRule;
import org.zanata.util.ZanataRestCaller;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ConcurrentAccessTest extends ZanataTestCase {

    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void concurrentDocumentCreationWillNotCauseHibernateException()
            throws InterruptedException {
        final String projectSlug = "project";
        final String iterationSlug = "master";
        final AtomicInteger counter = new AtomicInteger(1);
        new ZanataRestCaller().createProjectAndVersion(projectSlug,
                iterationSlug, "gettext");

        int threadCount = 5;
        Callable<Integer> task = new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                int suffix = counter.getAndIncrement();
                return new ZanataRestCaller().postSourceDocResource(
                        projectSlug, iterationSlug, buildResource(suffix),
                        false);
            }
        };
        List<Callable<Integer>> tasks = Collections.nCopies(threadCount, task);
        ExecutorService executorService =
                Executors.newFixedThreadPool(threadCount);

        List<Future<Integer>> futures = executorService.invokeAll(tasks);

        List<Integer> result = getStatusCodes(futures);

        List<Integer> expectedReturnCode =
                Collections.nCopies(threadCount, 201);
        assertThat(result, Matchers.equalTo(expectedReturnCode));
    }

    private static Resource buildResource(int suffix) {
        return buildSourceResource("doc" + suffix,
                buildTextFlow("res" + suffix, "content" + suffix));
    }

    private static List<Integer> getStatusCodes(List<Future<Integer>> futures) {
        return Lists.transform(futures,
                new Function<Future<Integer>, Integer>() {

                    @Override
                    public Integer apply(Future<Integer> input) {
                        return getResult(input);
                    }
                });
    }

    private static Integer getResult(Future<Integer> input) {
        try {
            return input.get();
        } catch (InterruptedException e) {
            throw Throwables.propagate(e);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }
}
