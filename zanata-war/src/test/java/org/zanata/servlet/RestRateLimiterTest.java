package org.zanata.servlet;

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
import org.zanata.servlet.RestRateLimiter;
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
        // Given: each thread will take 15ms to do its job
        final int timeSpendDoingWork = 15;

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
        RestRateLimiterTest.log.info("result: {}", timeUsedInMillis);
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

    @Test
    public void changeMaxConcurrentLimitWillTakeEffectImmediately() {
        rateLimiter =
                new RestRateLimiter(
                        new RestRateLimiter.RateLimitConfig(1, 1, 1));
        assertThat(rateLimiter.tryAcquireConcurrentPermit(), Matchers.is(true));
        assertThat(rateLimiter.tryAcquireConcurrentPermit(), Matchers.is(false));
        rateLimiter.changeConcurrentLimit(2);
        assertThat(rateLimiter.tryAcquireConcurrentPermit(), Matchers.is(true));
    }

    @Test
    public void changeMaxActiveLimitWhenNoBlockedThreads() {
        rateLimiter =
                new RestRateLimiter(
                        new RestRateLimiter.RateLimitConfig(3, 3, 1000));
        assertThat(acquireAndMeasureBlockedTime(), Matchers.equalTo(0L));
        rateLimiter.release();

        rateLimiter.changeActiveLimit(3, 2);
        assertThat(acquireAndMeasureBlockedTime(), Matchers.equalTo(0L));
        rateLimiter.release();

        rateLimiter.changeActiveLimit(2, 1);
        assertThat(acquireAndMeasureBlockedTime(), Matchers.equalTo(0L));
        assertThat(rateLimiter.availableActivePermit(), Matchers.is(0));
    }

    @Test
    public void changeMaxActiveLimitWhenHasBlockedThreads()
            throws InterruptedException {
        // Given: only 2 active requests allowed
        rateLimiter =
                new RestRateLimiter(
                        new RestRateLimiter.RateLimitConfig(10, 2, 1000));

        // When: below requests are fired simultaneously
        // 3 requests (each takes 20ms) and 1 request should block
        Callable<Long> callable = taskAcquireThenRelease(20);
        List<Callable<Long>> requests = Collections.nCopies(3, callable);
        // 1 task to update the change with 5ms delay
        // (so that it will happen while there is a blocked request)
        Callable<Long> changeTask = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                // to ensure it happens when there is a blocked request
                Uninterruptibles.sleepUninterruptibly(5, TimeUnit.MILLISECONDS);
                rateLimiter.changeActiveLimit(2, 3);
                return 1000L;
            }
        };
        // 2 delayed request that will try to acquire after the change
        // (while there is still a request blocking)
        Callable<Long> delayRequest = new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                // ensure this happen after change limit took place
                Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);
                long blockedTime = acquireAndMeasureBlockedTime();
                rateLimiter.release();
                return blockedTime;
            }
        };
        List<Callable<Long>> delayedRequests = Collections.nCopies(2, delayRequest);
        List<Callable<Long>> allTasks = Lists.newArrayList(requests);
        allTasks.add(changeTask);
        allTasks.addAll(delayedRequests);
        ExecutorService executorService = Executors.newFixedThreadPool(allTasks.size());
        List<Future<Long>> futures = executorService.invokeAll(allTasks);

        // Then: at the beginning 1 request should be blocked meanwhile change active limit will happen
        // the delayed request will wait until the blocked request finish and change the limit
        List<Long> timeUsedInMillis =
                getTimeUsedInMillisRoundedUpToTens(futures);

        RestRateLimiterTest.log.info("result: {}", timeUsedInMillis);
        // initial blocked thread's release will happen BEFORE change takes
        // effect (thus permit won't be added to changed semaphore)
        assertThat(rateLimiter.availableActivePermit(), Matchers.is(3));
        // 2 request with no block, 1 update request (indicated as 1000),
        // 1 initially blocked request, 2 delay requests blocked in which 1 is
        // responsible for making the change
        assertThat(timeUsedInMillis, Matchers.containsInAnyOrder(1000L, 0L, 0L, 20L, 30L, 30L));
    }

    // it will measure acquire blocking time and return it
    private Callable<Long> taskAcquireThenRelease(
            final int timeSpendDoingWorkInMillis) {
        return new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                long blockedTime = acquireAndMeasureBlockedTime();
                // spend some time doing some real work
                Uninterruptibles.sleepUninterruptibly(
                        timeSpendDoingWorkInMillis, TimeUnit.MILLISECONDS);
                rateLimiter.release();
                return blockedTime;
            }
        };
    }

    private long acquireAndMeasureBlockedTime() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        rateLimiter.acquire();
        stopwatch.stop();
        long blockedTime = stopwatch.elapsedMillis();
        RestRateLimiterTest.log.info("blocked: {}", blockedTime);
        return roundToTens(blockedTime);
    }

    private static List<Long> getTimeUsedInMillisRoundedUpToTens(
            List<Future<Long>> futures) {
        return Lists.transform(futures, new Function<Future<Long>, Long>() {
            @Override
            public Long apply(Future<Long> input) {
                try {
                    return roundToTens(input.get());
                } catch (Exception e) {
                    throw Throwables.propagate(e);
                }
            }
        });
    }

    private static long roundToTens(long arg) {
        return Math.round(arg / 10.0) * 10;
    }

}
