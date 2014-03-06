package org.zanata.rest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import lombok.extern.slf4j.Slf4j;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
@Slf4j
public class RestRateLimiterTest {
    private RestRateLimiter rateLimiter;
    private int maxConcurrent = 4;
    private int maxActive = 2;

    @BeforeMethod
    public void beforeMethod() {
        rateLimiter =
                new RestRateLimiter(new RestRateLimiter.RateLimitConfig(
                        maxConcurrent, maxActive, 1000.0));
    }

    @Test
    public void canOnlyHaveMaximumNumberOfConcurrentRequest() {
        for (int i = 0; i < maxConcurrent; i++) {
            assertThat(rateLimiter.tryAcquireConcurrentPermit(),
                    Matchers.is(true));
        }
        assertThat(rateLimiter.tryAcquireConcurrentPermit(), Matchers.is(false));
    }

    @Test
    public void canOnlyHaveMaxActiveConcurrentRequest()
            throws InterruptedException {
        // Given: each thread will take 10ms to do its job
        final int timeSpendDoingWork = 10;

        // When: max concurrent threads are accessing simultaneously
        Callable<Long> callable = taskAcquireThenRelease(timeSpendDoingWork);
        List<Callable<Long>> tasks =
                Collections.nCopies(maxConcurrent, callable);
        ExecutorService executorService =
                Executors.newFixedThreadPool(maxConcurrent);
        List<Future<Long>> futures = executorService.invokeAll(tasks);

        // Then: only max active threads will be served immediately while others
        // will block until them finish
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);
        log.info("result: {}", timeUsedInMillis);
        Iterable<Long> blocked =
                Iterables.filter(timeUsedInMillis, new Predicate<Long>() {
                    @Override
                    public boolean apply(Long input) {
                        return input > 0;
                    }
                });
        assertThat(blocked,
                Matchers.<Long> iterableWithSize(maxConcurrent - maxActive));
    }

    @Test
    public void activeRequestWillBeRateLimited() throws InterruptedException {
        // Given: I am within max active threads count and can do my job RIGHT
        // NOW and permits per second is 1
        final int timeSpendDoingWork = 0;
        double permitsPerSecond = 1;
        rateLimiter.changeRateLimitPermitsPerSecond(permitsPerSecond);

        // When: I start working on heavy duty stuff
        Callable<Long> callable = taskAcquireThenRelease(timeSpendDoingWork);
        List<Callable<Long>> tasks = Collections.nCopies(maxActive, callable);
        ExecutorService executorService =
                Executors.newFixedThreadPool(maxActive);
        List<Future<Long>> futures = executorService.invokeAll(tasks);

        // Then: I get rate limited (＃｀д´)ﾉ
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);
        Iterable<Long> blocked =
                Iterables.filter(timeUsedInMillis, new Predicate<Long>() {
                    @Override
                    public boolean apply(Long input) {
                        return input > 0;
                    }
                });
        assertThat(blocked, Matchers.<Long> iterableWithSize(1));
    }

    // it will measure acquire blocking time and return it
    private Callable<Long> taskAcquireThenRelease(
            final int timeSpendDoingWorkInMillis) {
        return new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                Stopwatch stopwatch = new Stopwatch();
                stopwatch.start();
                rateLimiter.acquire();
                stopwatch.stop();
                long blockedTime = stopwatch.elapsedMillis();
                log.info("blocked: {}", blockedTime);
                // spend some time doing some real work
                Uninterruptibles.sleepUninterruptibly(
                        timeSpendDoingWorkInMillis, TimeUnit.MILLISECONDS);
                rateLimiter.release();
                return blockedTime;
            }
        };
    }

    private static List<Long> getTimeUsedInMillisRoundedUpToTens(
            List<Future<Long>> futures) {
        return Lists.transform(futures, new Function<Future<Long>, Long>() {
            @Override
            public Long apply(Future<Long> input) {
                try {
                    return input.get() / 10 * 10;
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        });
    }

}
