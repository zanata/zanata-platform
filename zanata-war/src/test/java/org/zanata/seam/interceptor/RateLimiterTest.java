package org.zanata.seam.interceptor;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hamcrest.Matchers;
import org.jboss.seam.intercept.InvocationContext;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
@Slf4j
public class RateLimiterTest {
    public static final int CAPACITY_TOKENS = 1000;
    private RateLimiter rateLimiter;
    @Mock
    private InvocationContext invocationContext;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        rateLimiter = new RateLimiter(CAPACITY_TOKENS, "api");
        TokenBucketsHolder.HOLDER.invalidateAll();
    }

    @Test
    public void longRunningRequestWillBlockSubsequentRequest() throws Exception {
        // Given: a long running request (takes 1000ms) come in with fresh
        // bucket with capacity 1000
        when(invocationContext.proceed()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(1000);
                return null;
            }
        });

        // When: the request finishes
        InvocationContextMeasurer measurer =
                InvocationContextMeasurer.of(invocationContext);
        rateLimiter.consume(measurer);

        // TODO no minus refill. just log it so that we can tune the perConsumption

        // Then: the bucket will be empty and subsequent request
        // will block
        assertThat(TokenBucketsHolder.HOLDER.getIfPresent("api").currentSize(),
                Matchers.equalTo(0L));
        Stopwatch stopWatch = new Stopwatch();
        stopWatch.start();
        rateLimiter.consume(InvocationContextMeasurer.of(
                mock(InvocationContext.class)));
        stopWatch.stop();
        log.info("blocked: {}", stopWatch.elapsedMillis());
        assertThat(stopWatch.elapsedMillis(),
                Matchers.greaterThanOrEqualTo(500L));
    }

    @Test
    public void quickRequestCanExecuteSequentiallyWithMinorDelay()
            throws Exception {
        // Given: a quick request come in with fresh bucket
        InvocationContextMeasurer measurer =
                InvocationContextMeasurer.of(invocationContext);

        // When:
        rateLimiter.consume(measurer);

        // Then: the bucket will be refilled nearly full and subsequent request
        // can be served immediately
        assertThat(TokenBucketsHolder.HOLDER.getIfPresent("api").currentSize(),
                Matchers.greaterThan(990L));
    }

    @Test
    public void canOnlySupportLimitedConcurrentRequests() throws Exception {
        // Given: 3 request(takes 200ms) come in with fresh bucket
        when(invocationContext.proceed()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(200);
                return null;
            }
        });

        // When:
        Callable<Long> task = new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                InvocationContextMeasurer measurer =
                        InvocationContextMeasurer.of(invocationContext);
                Stopwatch stopwatch = new Stopwatch();
                stopwatch.start();
                rateLimiter.consume(measurer);
                stopwatch.stop();
                return stopwatch.elapsedMillis();
            }
        };
        List<Callable<Long>> tasks = Collections.nCopies(3, task);
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<Future<Long>> futures = executorService.invokeAll(tasks);

        // Then: 2 will be served immediately and 3nd one will block a bit until
        // one of the 2 finishes
        List<Long> timeUsedInMillis = getTimeUsedInMillis(futures);
        log.info("time used: {}", timeUsedInMillis);
        assertThat(timeUsedInMillis,
                Matchers.containsInAnyOrder(200L, 200L, 400L));

    }

    private static List<Long> getTimeUsedInMillis(List<Future<Long>> futures) {
        return Lists.transform(futures,
                new Function<Future<Long>, Long>() {
                    @Override
                    public Long apply(Future<Long> input) {
                        try {
                            return input.get() / 100 * 100;
                        }
                        catch (Exception e) {
                            throw Throwables.propagate(e);
                        }
                    }
                });
    }
}
